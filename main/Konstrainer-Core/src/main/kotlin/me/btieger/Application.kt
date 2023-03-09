package me.btieger

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.services.cronjobs.Cleaner
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureSerialization
import me.btieger.services.DslService
import me.btieger.services.DslServiceImpl
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level


@Suppress("ClassName")
object config : EnvVarSettings("KSR_") {
    val serviceName by string()
    val builderImage by string("tiegris/konstrainer-builder:dev")
    val builderJobTtlMinutes by int(1)
    val namespace by string("konstrainer-ns")
    val port by int(8080)
    val host by string("0.0.0.0")
    val cleanerIntervalSeconds by int(5*60)

    init {
        loadAll()
    }
}


fun main() {
    embeddedServer(Netty,
        port = config.port,
        host = config.host,
        module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    val k8sClient = KubernetesClientBuilder().build()
    install(Koin) {
        modules(
            module {
                single<KubernetesClient> { k8sClient }
                single<DslService> { DslServiceImpl(k8sClient) }
            }
        )
    }
    install(CallLogging) {
        level = Level.INFO
    }
    configureSerialization()

    echoController()
    dslController()
    //serverController()

    DatabaseFactory.init()

    launch {
        val interval = config.cleanerIntervalSeconds * 1000L
        val cleaner = Cleaner(k8sClient)
        while(true) {
            delay(interval)
            cleaner.runCleaner()
        }
    }
}
