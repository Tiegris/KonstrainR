package me.btieger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.btieger.loader.Loader
import me.btieger.plugins.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
import java.security.KeyStore


fun main() {
    val passwd = "foobar".toCharArray()
    val keyStoreFilePath = "build/keystore.jks"
    val keyStoreFile = File(keyStoreFilePath)
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }

    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keyStore,
            keyAlias = "AgentCert",
            keyStorePassword = { passwd },
            privateKeyPassword = { passwd }) {
            keyStorePath = keyStoreFile
            port = 8443
            host = "0.0.0.0"
        }
        module(Application::module)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    val rulesJarLocation = System.getenv("KSR_RULES_JAR_PATH") ?: "/app/ruleset.jar"
    val ruleset = Loader("DslInstanceKt").loadServer(rulesJarLocation)
    configureHTTP()
    configureSerialization()
    configureAdministration()
    configureRouting(ruleset)
}
