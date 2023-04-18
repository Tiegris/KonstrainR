package me.btieger

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.controllers.serverController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureSerialization
import me.btieger.services.*
import me.btieger.services.cronjobs.launchCleaner
import me.btieger.services.ssl.SslServiceOpenSslWrapperImpl
import me.btieger.services.ssl.SslService
import me.btieger.services.ssl.SslServiceMockImpl
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level


object Config : EnvVarSettings("KSR_") {
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
        port = Config.port,
        host = Config.host,
        module = Application::startup)
        .start(wait = true)
}


fun Application.startup() {
    val k8sClient = KubernetesClientBuilder().build()
    install(Koin) {
        modules(
            module {
                single<KubernetesClient> { k8sClient }
                single<DslService> { DslServiceImpl(k8sClient) }
                if (environment.developmentMode)
                    single<SslService> { SslServiceMockImpl() }
                else
                    single<SslService> { SslServiceOpenSslWrapperImpl() }

                single<ServerService> { ServerServiceImpl(this@startup) }
            }
        )
    }
    install(CallLogging) {
        level = Level.INFO
    }
    configureSerialization()

    echoController()
    dslController()
    serverController()

    DatabaseFactory.init()

    launchCleaner()
}
