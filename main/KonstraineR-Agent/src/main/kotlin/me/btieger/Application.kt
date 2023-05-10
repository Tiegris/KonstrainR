package me.btieger

import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.btieger.plugins.configureAdministration
import me.btieger.plugins.configureHTTP
import me.btieger.plugins.configureRouting
import me.btieger.plugins.configureSerialization
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import io.ktor.server.plugins.callloging.*
import me.btieger.loader.Loader
import org.slf4j.event.Level
import java.nio.file.Paths

class Config : EnvVarSettings("KSR_") {
    val rootDir by string("/app")
    val development by boolean(false)

    init {
        loadAll()
    }
}
private val _config = Config()

suspend fun main() {
    val logger = LoggerFactory.getLogger("ktor.application")
    val passwd = "foobar".toCharArray()
    val keyStoreFilePath = "${_config.rootDir}/keystore.jks"
    val keyStoreFile = File(keyStoreFilePath)
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    withContext(Dispatchers.IO) {
        FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }
    }

    val environment = applicationEngineEnvironment {
        log = logger
        if (_config.development) {
            connector {
                port = 8081
            }
        } else {
            sslConnector(
                keyStore = keyStore,
                keyAlias = "AgentCert",
                keyStorePassword = { passwd },
                privateKeyPassword = { passwd }) {
                keyStorePath = keyStoreFile
                port = 8443
                host = "0.0.0.0"
            }
        }
        module(Application::module)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }
    val debugServer = server
    //val debugServer = me.btieger.builtins.server
    val ruleset = if (_config.development) debugServer else Loader("me.btieger.DslInstanceKt").loadServer(Paths.get("/app/libs/agentdsl.jar"))
    configureHTTP()
    configureSerialization()
    configureAdministration()
    configureRouting(ruleset, KubernetesClientBuilder().build())
}
