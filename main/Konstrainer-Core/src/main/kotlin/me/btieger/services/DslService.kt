package me.btieger.services

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.util.logging.*
import me.btieger.Config
import me.btieger.bytesStable
import me.btieger.domain.*
import me.btieger.loader.Loader
import me.btieger.services.helm.HelmService
import me.btieger.services.helm.create
import me.btieger.services.helm.resources.makeBuilderJob
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.DatabaseFactory.dbQuery
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.BuildStatus
import me.btieger.persistance.tables.ServerStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

interface DslService {
    suspend fun all(): List<DslConciseDto>
    suspend fun allWithAggregators(): List<String>
    suspend fun getJar(id: Int): ByteArray?
    suspend fun getFile(id: Int): ByteArray?
    suspend fun getDetails(id: Int): DslDetailedDto?
    suspend fun getFullDetails(id: Int): DslFullDetailedDto?
    suspend fun createDsl(name: String, content: ByteArray): DslDetailedDto?
    suspend fun setJar(id: Int, secret: String, jarBytes: ByteArray)
    suspend fun setError(id: Int, secret: String, errorMessage: String)
    suspend fun deleteDsl(id: Int): Boolean
}

class DslServiceImpl(
    private val kubectl: KubernetesClient,
    private val config: Config,
    private val helm: HelmService,
    private val serverService: ServerService,
) : DslService {
    private val logger: Logger = LoggerFactory.getLogger("DslService")!!

    override suspend fun all() = dbQuery {
        Dsl.all().map(Dsl::toConciseDto)
    }

    override suspend fun allWithAggregators() = dbQuery {
        Dsl.all().filter {( it.hasMonitors == true) and (it.serverStatus == ServerStatus.Up) }.map { it.name }
    }

    override suspend fun getJar(id: Int) = dbQuery {
        // jar.bytes throws exception, since Kotlin version update
        Dsl.findById(id)?.jar?.let { it.bytesStable }
    }

    override suspend fun getFile(id: Int) = dbQuery {
        // jar.bytes throws exception, since Kotlin version update
        Dsl.findById(id)?.let { it.file.bytesStable }
    }

    override suspend fun getDetails(id: Int) = dbQuery {
        Dsl.findById(id)?.let(Dsl::toDetailedDto)
    }

    override suspend fun getFullDetails(id: Int) = dbQuery {
        Dsl.findById(id)?.let(Dsl::toFullDetailedDto)
    }

    override suspend fun createDsl(name: String, content: ByteArray) = dbQuery {
        val secretBytes = ByteArray(64) // 512 bit token
        SecureRandom().nextBytes(secretBytes)
        val secretEncoded = String(Base64.getEncoder().encode(secretBytes))
        // TODO review token security

        val result = Dsl.new {
            this.name = name
            this.file = ExposedBlob(content)
            this.buildStatus = BuildStatus.Building
            this.jobSecret = secretBytes
            this.buildSubmissionTime = LocalDateTime.now()
        }
        val builderJob = helm.makeBuilderJob(result.id.value, secretEncoded)
        try {
            kubectl.create(builderJob)
        } catch (e: Exception) {
            logger.error("Failed to spawn k8s job")
            throw e
        }
        logger.info("Started k8s job named: `{}` in the namespace: `{}`", "${builderJob.fullResourceName}/${builderJob.metadata.name}", builderJob.metadata.namespace)
        result.toDetailedDto()
    }

    override suspend fun setJar(id: Int, secret: String, jarBytes: ByteArray): Unit = dbQuery {
        val secretBytes = Base64.getDecoder().decode(secret)
        //TODO review security

        val server = Loader("me.btieger.DslInstanceKt").loadServer(jarBytes)

        val success = Dsls.update({ Dsls.id eq id and Dsls.jar.isNull() /*and (Dsls.jobSecret eq secretBytes) */ }) {
            it[name] = server.name
            it[jar] = ExposedBlob(jarBytes)
            it[buildSubmissionTime] = null
            it[errorMessage] = null
            it[jobSecret] = null
            it[buildStatus] = BuildStatus.Ready
            it[hasMonitors] = server.monitors.isNotEmpty()
            it[hasWebhooks] = server.webhooks.isNotEmpty()
        } > 0
        if (!success)
            logger.warn("Failed JAR upload attempt, dsl.id: `{}`", id)
        else
            logger.info("Successful JAR upload, dsl.id: `{}`", id)
    }

    override suspend fun setError(id: Int, secret: String, errorMessage: String): Unit = dbQuery {
        val secretBytes = Base64.getDecoder().decode(secret)
        val success= Dsls.update({ (Dsls.id eq id) and Dsls.jar.isNull() and (Dsls.jobSecret eq secretBytes) }) {
            it[Dsls.errorMessage] = errorMessage
            it[buildStatus] = BuildStatus.Failed
            it[jobSecret] = null
        } > 0
        if (!success)
            logger.warn("Failed build error report upload attempt, dsl.id: `{}`", id)
        else
            logger.info("Successful build error report upload, dsl.id: `{}`", id)
    }

    override suspend fun deleteDsl(id: Int) = dbQuery {
        serverService.stop(id)
        val success = Dsls.deleteWhere { Dsls.id eq id } > 0
        if (!success)
            logger.info("Failed delete dsl, dsl.id: `{}`", id)
        else
            logger.info("Deleted dsl, dsl.id: `{}`", id)
        success
    }

}
