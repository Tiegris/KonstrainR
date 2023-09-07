package me.btieger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.webjars.*
import me.btieger.commonLibrary.EnvVarSettings
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.controllers.serverController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureAuthentication
import me.btieger.plugins.configureKoin
import me.btieger.plugins.configureSerialization
import me.btieger.plugins.initSsl
import me.btieger.services.cronjobs.launchCleaner
import me.btieger.services.ssl.SslClient
import me.btieger.webui.configureCSS
import me.btieger.webui.configurePagesRouting
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File


class Config : EnvVarSettings("KSR_") {
    val serviceName by string()
    val builderImage by string("tiegris/konstrainer-builder:dev")
    val builderJobTtlMinutes by int(1)
    val namespace by string("konstrainer-ns")
    val port by int(8080)
    val host by string("0.0.0.0")
    val cleanerIntervalSeconds by int(5 * 60)
    val agentImage by string("tiegris/konstrainer-agent:0.0.1")
    val agentSpawnWaitSeconds by long(5L)
    val agentSpawnWaitMaxRetries by int(5)
    val enableWebUi by boolean(true)
    val home by string("/app/home")
    val adminUser by string("admin")
    val adminPass by string("admin")
    val agentUser by string("alma")
    val agentPass by string("alma")
    val developmentMode by boolean(false)
}

private val _config = Config().apply(Config::loadAll)

fun main() {

    if (_config.developmentMode) {
        embeddedServer(
            Netty,
            port = 8080,
            host = "localhost",
            module = Application::startup
        )
            .start(wait = true)
    } else {
        val sslClient = SslClient(File(_config.home))
        val environment = applicationEngineEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
            sslConnector(
                keyStore = sslClient.keyStore,
                keyAlias = "RootCA",
                keyStorePassword = { sslClient.passwd },
                privateKeyPassword = { sslClient.passwd }) {
                keyStorePath = sslClient.keyStoreFile
                port = _config.port
                host = _config.host
            }
            module(Application::startup)
        }
        embeddedServer(Netty, environment).start(wait = true)
    }

}


fun Application.startup() {
    install(CallLogging) {
        level = Level.INFO
    }

    configureKoin(_config)
    DatabaseFactory.init(_config)

    initSsl()

    configureSerialization()
    configureAuthentication(_config)

    echoController()
    dslController()
    serverController()

    if (_config.enableWebUi) {
        install(Webjars) {
            path = "assets"
        }
        configureCSS()
        configurePagesRouting()
    }

    launchCleaner()
}
