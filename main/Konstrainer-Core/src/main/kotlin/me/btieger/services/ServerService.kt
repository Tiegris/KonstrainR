package me.btieger.services

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.btieger.Config
import me.btieger.bytesStable
import me.btieger.logic.kelm.HelmService
import me.btieger.logic.kelm.resources.secret
import me.btieger.persistance.DatabaseFactory
import me.btieger.logic.kelm.create
import me.btieger.logic.kelm.resources.deployment
import me.btieger.logic.kelm.resources.mutatingWebhookConfiguration
import me.btieger.logic.kelm.resources.service
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.ServerStatus
import me.btieger.persistance.tables.BuildStatus
import me.btieger.services.ssl.SslService
import org.koin.ktor.ext.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

interface ServerService {
    suspend fun start(id: Int)
    suspend fun stop(id: Int)
}

class ServerServiceImpl(
    private val sslService: SslService,
    private val k8sclient: KubernetesClient,
    private val config: Config,
    private val helm: HelmService,
) : ServerService {
    private val logger: Logger = LoggerFactory.getLogger("ServerService")!!

    private suspend fun getDsl(id: Int) = DatabaseFactory.dbQuery {
        val dsl = Dsl.findById(id) ?: throw NotFoundException()
        if (dsl.buildStatus != BuildStatus.Ready || dsl.jar == null) throw IllegalStateException("Dsl not ready")
        if (dsl.serverStatus == ServerStatus.Up) throw IllegalStateException("Server is already up")
        if (dsl.serverStatus == ServerStatus.Spawning) throw IllegalStateException("Server already spawning")
        return@dbQuery dsl.jar!!.bytesStable
    }

    private suspend fun setDslStatus(id: Int, status: ServerStatus) = DatabaseFactory.dbQuery {
        val dsl = Dsl.findById(id) ?: throw NotFoundException()
        dsl.serverStatus = status
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun start(id: Int) {
        val dsl = getDsl(id)

        setDslStatus(id, ServerStatus.Spawning)
        logger.info("Launching server spawner coroutine, dsl.id: `{}`", id)
        GlobalScope.launch {
            try {
                // read server conf from db
                //val server = Loader("DslInstanceKt").loadServer(dsl)
                val server = me.btieger.server

                val cname = "${server.whName}.${config.namespace}.svc"

                val cert = sslService.deriveCert(cname)
                val secret = helm.secret(server, cert)
                k8sclient.resource(secret).inNamespace(config.namespace).create()

                val dep = helm.deployment(server)
                val svc = helm.service(server)

                k8sclient.create(dep)
                k8sclient.create(svc)

                k8sclient.services().withName(svc.fullResourceName).waitUntilReady(5, TimeUnit.SECONDS)


                val ca = sslService.getRootCaAsPem()
                val mwhc = helm.mutatingWebhookConfiguration(server, ca)
                k8sclient.create(mwhc)

                setDslStatus(id, ServerStatus.Up)
                logger.info("Server started, dsl.id: `{}`", id)
            } catch (e: Throwable) {
                logger.error("Error when spawning server, dsl.id: `{}`, message: `{}`", id, e.message)
                logger.error(e.stackTraceToString())
                setDslStatus(id, ServerStatus.Error)
            }
        }

    }



    override suspend fun stop(id: Int) {
        TODO("Not yet implemented")
    }


}
