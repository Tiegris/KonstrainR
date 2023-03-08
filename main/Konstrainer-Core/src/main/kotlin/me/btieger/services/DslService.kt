package me.btieger.services

import io.ktor.util.logging.*
import me.btieger.domain.DslConciseDto
import me.btieger.domain.DslDetailedDto
import me.btieger.domain.toConciseDto
import me.btieger.domain.toDetailedDto
import me.btieger.logic.kelm.kubectl
import me.btieger.logic.kelm.resources.makeBuilderJob
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.Status
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

interface DslService {
    suspend fun all(): List<DslConciseDto>
    suspend fun getJar(id: Int): ByteArray?
    suspend fun getFile(id: Int): ByteArray?
    suspend fun getDetails(id: Int): DslDetailedDto?
    suspend fun createDsl(name: String, content: ByteArray): DslDetailedDto?
    suspend fun setJar(id: Int, secret: String, jarBytes: ByteArray)
    suspend fun setError(id: Int, secret: String, errorMessage: String)
    suspend fun deleteDsl(id: Int): Boolean
}

class DslServiceImpl : DslService {
    val logger = LoggerFactory.getLogger("DslService")

    override suspend fun all() = DatabaseFactory.dbQuery {
        Dsl.all().map(Dsl::toConciseDto)
    }

    override suspend fun getJar(id: Int) = DatabaseFactory.dbQuery {
        Dsl.findById(id)?.jar?.let { it.inputStream.readAllBytes() }
    }

    override suspend fun getFile(id: Int) = DatabaseFactory.dbQuery {
        Dsl.findById(id)?.let { it.file.inputStream.readAllBytes() }
    }

    override suspend fun getDetails(id: Int) = DatabaseFactory.dbQuery {
        Dsl.findById(id)?.let(Dsl::toDetailedDto)
    }

    override suspend fun createDsl(name: String, content: ByteArray) = DatabaseFactory.dbQuery {
        val secretBytes = ByteArray(64) // 512 bit token
        SecureRandom().nextBytes(secretBytes)
        val secretEncoded = String(Base64.getEncoder().encode(secretBytes))
        // TODO review token security

        val result = Dsl.new {
            this.name = name
            this.file = ExposedBlob(content)
            this.status = Status.Building
            this.jobSecret = secretBytes
            this.buildSubmissionTime = LocalDateTime.now()
        }
        val builderJob = makeBuilderJob(result.id.value, secretEncoded)
        try {
            kubectl.create(builderJob)
        } catch (e: Exception) {
            logger.error("Failed to spawn k8s job")
            throw e
        }
        logger.info("Started k8s job named: `{}` in the namespace: `{}`", "${builderJob.fullResourceName}/${builderJob.metadata.name}", builderJob.metadata.namespace)
        result.toDetailedDto()
    }

    override suspend fun setJar(id: Int, secret: String, jarBytes: ByteArray): Unit = DatabaseFactory.dbQuery {
        val secretBytes = Base64.getDecoder().decode(secret)
        val success = Dsls.update({ Dsls.id eq id and Dsls.jar.isNull() and (Dsls.jobSecret eq secretBytes) }) {
            it[jar] = ExposedBlob(jarBytes)
            it[buildSubmissionTime] = null
            it[errorMessage] = null
            it[jobSecret] = null
            it[status] = Status.Ready
        } > 0
        if (!success)
            logger.warn("Failed JAR upload attempt, dsl.id: `{}`", id)
        else
            logger.info("Successful JAR upload, dsl.id: `{}`", id)
    }

    override suspend fun setError(id: Int, secret: String, errorMessage: String): Unit = DatabaseFactory.dbQuery {
        val secretBytes = Base64.getDecoder().decode(secret)
        val success= Dsls.update({ (Dsls.id eq id) and Dsls.jar.isNull() and (Dsls.jobSecret eq secretBytes) }) {
            it[Dsls.errorMessage] = errorMessage
            it[status] = Status.Failed
            it[jobSecret] = null
        } > 0
        if (!success)
            logger.warn("Failed build error report upload attempt, dsl.id: `{}`", id)
        else
            logger.info("Successful build error report upload, dsl.id: `{}`", id)
    }

    override suspend fun deleteDsl(id: Int) = DatabaseFactory.dbQuery {
        val success = Dsls.deleteWhere { Dsls.id eq id } > 0
        if (!success)
            logger.info("Failed delete dsl, dsl.id: `{}`", id)
        else
            logger.info("Deleted dsl, dsl.id: `{}`", id)
        success
    }

}

val dslService: DslService = DslServiceImpl()
