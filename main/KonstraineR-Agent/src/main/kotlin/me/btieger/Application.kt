package me.btieger

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.btieger.plugins.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate


fun main() {
    val passwd = "foobar".toCharArray()
    val keyStoreFile = "build/keystore.jks"
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }

    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keyStore,
            keyAlias = "sampleAlias",
            keyStorePassword = { passwd },
            privateKeyPassword = { passwd }) {
            port = 8443
            host = "0.0.0.0"
        }
        module(Application::module)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureAdministration()
    configureRouting(server)
}
