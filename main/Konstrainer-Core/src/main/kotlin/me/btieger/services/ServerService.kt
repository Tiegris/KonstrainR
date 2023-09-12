package me.btieger.services

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientTimeoutException
import io.ktor.server.plugins.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.btieger.Config
import me.btieger.bytesStable
import me.btieger.loader.Loader
import me.btieger.services.helm.HelmService
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.DatabaseFactory.dbQuery
import me.btieger.services.helm.create
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.ServerStatus
import me.btieger.persistance.tables.BuildStatus
import me.btieger.persistance.tables.Dsls
import me.btieger.services.helm.resources.*
import me.btieger.services.ssl.SslService
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

interface ServerService {
    suspend fun start(id: Int)
    suspend fun stop(id: Int)
    suspend fun stopAll()
}

class ServerServiceImpl(
    private val sslService: SslService,
    private val k8sclient: KubernetesClient,
    private val config: Config,
    private val helm: HelmService,
) : ServerService {
    private val logger: Logger = LoggerFactory.getLogger("ServerService")!!

    private suspend fun getDsl(id: Int) = dbQuery {
        val dsl = Dsl.findById(id) ?: throw NotFoundException()
        if (dsl.buildStatus != BuildStatus.Ready || dsl.jar == null) throw IllegalStateException("Dsl not ready")
        if (dsl.serverStatus == ServerStatus.Up) throw IllegalStateException("Server is already up")
        if (dsl.serverStatus == ServerStatus.Spawning) throw IllegalStateException("Server already spawning")
        return@dbQuery dsl.jar!!.bytesStable
    }

    private suspend fun setDslStatus(id: Int, status: ServerStatus) = dbQuery {
        val dsl = Dsl.findById(id) ?: throw NotFoundException()
        dsl.serverStatus = status
    }

    private suspend fun setAllDown() = dbQuery {
        Dsls.update {
            it[Dsls.serverStatus] = ServerStatus.Down
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun start(id: Int) {
        val dsl = getDsl(id)

        setDslStatus(id, ServerStatus.Spawning)
        logger.info("Launching server spawner coroutine, dsl.id: `{}`", id)
        GlobalScope.launch {
            try {
                val server = Loader("me.btieger.DslInstanceKt").loadServer(dsl)

                val cname = "${server.name}.${config.namespace}.svc"
                val altnames = listOf(
                    "${server.name}.${config.namespace}.svc",
                    "${server.name}.${config.namespace}",
                    server.name
                )

                val cert = sslService.deriveCert(cname, altnames)
                val secret = helm.secret(server, cert, sslService.rootCa, id)
                k8sclient.create(secret)

                val rbac = helm.rbac(server, id)
                val dep = helm.deployment(server, id)
                val svc = helm.service(server, id)
                rbac?.let {
                    k8sclient.create(it.serviceAccount)
                    k8sclient.create(it.clusterRoleBinding)
                    dep.spec.template.spec.serviceAccountName = server.name
                }

                k8sclient.create(dep)
                k8sclient.create(svc)
                logger.info("Waiting for agent to start, dsl.id: `{}`", id)
                var retryCounter = 0
                while (true) {
                    try {
                        k8sclient.resource(dep).waitUntilCondition({
                            (it.status.availableReplicas ?: 0) > 0
                        }, config.agentSpawnWaitSeconds, TimeUnit.SECONDS)
                        break
                    } catch (e: KubernetesClientTimeoutException) {
                        retryCounter++
                        if (retryCounter > config.agentSpawnWaitMaxRetries)
                            throw e
                        logger.warn("Still waiting for agent to start, dsl.id: `{}`, retries: `{}`", id, retryCounter)
                    }
                }
                logger.info("Agent to started, dsl.id: `{}`", id)

                val ca = sslService.rootCa
                val mwhc = helm.mutatingWebhookConfiguration(server, ca, id)
                k8sclient.create(mwhc)

                setDslStatus(id, ServerStatus.Up)
                logger.info("Server started, dsl.id: `{}`", id)
            } catch (e: Throwable) {
                logger.error("Error when spawning server, dsl.id: `{}`, message: `{}`", id, e.message)
                logger.error(e.stackTraceToString())
                setDslStatus(id, ServerStatus.Error)
                if (delete(id))
                    logger.info("Successful rollback, dsl.id: `{}`", id)
            }
        }

    }

    private fun delete(id: Int) = try {
            k8sclient.secrets().inNamespace(config.namespace).withLabel("agentId", "$id").delete()
            k8sclient.apps().deployments().inNamespace(config.namespace).withLabel("agentId", "$id").delete()
            k8sclient.services().inNamespace(config.namespace).withLabel("agentId", "$id").delete()
            k8sclient.serviceAccounts().inNamespace(config.namespace).withLabel("agentId", "$id").delete()
            k8sclient.rbac().clusterRoleBindings().withLabel("agentId", "$id").delete()
            k8sclient.admissionRegistration().v1().mutatingWebhookConfigurations().withLabel("agentId", "$id").delete()
            true
        } catch (e: Throwable) {
            logger.error("Error deleting agent, dsl.id: `{}`, message: `{}`", id, e.message)
            logger.error(e.stackTraceToString())
            false
        }


    override suspend fun stop(id: Int) {
        if (delete(id)) {
            logger.info("Successful rollback, dsl.id: `{}`", id)
            setDslStatus(id, ServerStatus.Down)
        } else {
            setDslStatus(id, ServerStatus.Error)
        }
    }

    override suspend fun stopAll() {
        logger.info("All rules deleted.")
        k8sclient.secrets().inNamespace(config.namespace).withLabel("managedBy", "konstrainer").delete()
        k8sclient.apps().deployments().inNamespace(config.namespace).withLabel("managedBy", "konstrainer").delete()
        k8sclient.services().inNamespace(config.namespace).withLabel("managedBy", "konstrainer").delete()
        k8sclient.serviceAccounts().inNamespace(config.namespace).withLabel("managedBy", "konstrainer").delete()
        k8sclient.rbac().clusterRoleBindings().withLabel("managedBy", "konstrainer").delete()
        k8sclient.admissionRegistration().v1().mutatingWebhookConfigurations().withLabel("managedBy", "konstrainer").delete()
        setAllDown()
    }


}
