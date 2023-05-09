package me.btieger

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.webjars.*
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.controllers.serverController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureKoin
import me.btieger.plugins.configureSerialization
import me.btieger.services.*
import me.btieger.services.cronjobs.launchCleaner
import me.btieger.services.ssl.SslServiceOpenSslWrapperImpl
import me.btieger.services.ssl.SslService
import me.btieger.services.ssl.SslServiceMockImpl
import me.btieger.webui.configureCSS
import me.btieger.webui.configurePagesRouting
import me.btieger.webui.pages.configureAggregationsPageController
import me.btieger.webui.pages.configureServersPageController
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level


class Config : EnvVarSettings("KSR_") {
    val serviceName by string()
    val builderImage by string("tiegris/konstrainer-builder:dev")
    val builderJobTtlMinutes by int(1)
    val namespace by string("konstrainer-ns")
    val port by int(8080)
    val host by string("0.0.0.0")
    val cleanerIntervalSeconds by int(5*60)
    val agentImage by string("tiegris/konstrainer-agent:0.0.1")
    val agentSpawnWaitSeconds by long(5L)
    val agentSpawnWaitMaxRetries by int(5)
    val enableWebUi by boolean(true)
    val home by string("/app/home")

    init {
        loadAll()
    }
}
private val config = Config()

fun main() {
    embeddedServer(Netty,
        port = config.port,
        host = config.host,
        module = Application::startup)
        .start(wait = true)
}


fun Application.startup() {
    install(CallLogging) {
        level = Level.INFO
    }

    configureSerialization()
    configureKoin(config)

    echoController()
    dslController()
    serverController()

    DatabaseFactory.init(config)

    if (config.enableWebUi) {
        install(Webjars) {
            path = "assets"
        }

        configureCSS()
        configurePagesRouting()
    }

    launchCleaner()
}
