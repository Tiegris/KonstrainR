package me.btieger

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.btieger.loader.Loader
import me.btieger.plugins.configureAdministration
import me.btieger.plugins.configureHTTP
import me.btieger.plugins.configureRouting
import me.btieger.plugins.configureSerialization
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

class Config : EnvVarSettings("KSR_") {
    val coreBaseUrl by string()
    val dslId by int()

    init {
        loadAll()
    }
}
private val config = Config()

suspend fun main() {
    val passwd = "foobar".toCharArray()
    val keyStoreFilePath = "/app/keystore.jks"
    val keyStoreFile = File(keyStoreFilePath)
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    withContext(Dispatchers.IO) {
        FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }
    }

    HttpClient(CIO).use {
        val url = "http://${config.coreBaseUrl}:8080/api/v1/dsls"
        val response: HttpResponse = it.request("$url/${config.dslId}")
        File("/app/ruleset.jar").writeBytes(response.readBytes()) // TODO optimise this
    }

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
    val ruleset = Loader("DslInstanceKt").loadServer("/app/ruleset.jar")
    configureHTTP()
    configureSerialization()
    configureAdministration()
    configureRouting(ruleset)
}
