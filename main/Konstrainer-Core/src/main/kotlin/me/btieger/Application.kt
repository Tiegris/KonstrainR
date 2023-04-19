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
import me.btieger.plugins.configureKoin
import me.btieger.plugins.configureSerialization
import me.btieger.services.*
import me.btieger.services.cronjobs.launchCleaner
import me.btieger.services.ssl.SslServiceOpenSslWrapperImpl
import me.btieger.services.ssl.SslService
import me.btieger.services.ssl.SslServiceMockImpl
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
    val agentImage by string("tiegris/konstrainer-agent:latest")

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

    DatabaseFactory.init()

    launchCleaner()
}
