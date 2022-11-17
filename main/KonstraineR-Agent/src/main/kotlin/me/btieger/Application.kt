package me.btieger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.btieger.plugins.*
import java.io.File
import io.ktor.network.tls.certificates.*
import me.btieger.dsl.Rule
import me.btieger.dsl.Server
import me.btieger.dsl.Status
import me.btieger.dsl.WhConf

fun main() {
    //val keyStoreFile = File("pems/keystore.jks")

    val keyStoreFile = File("build/keystore.jks")
    val keystore = generateCertificate(
        file = keyStoreFile,
        keyAlias = "sampleAlias",
        keyPassword = "foobar",
        jksPassword = "foobar"
    )

    val environment = applicationEngineEnvironment {
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keystore,
            keyAlias = "sampleAlias",
            keyStorePassword = { "foobar".toCharArray() },
            privateKeyPassword = { "foobar".toCharArray() }) {
            port = 8443
        }
        module(Application::module)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureAdministration()
    val srv = Server("asd", "asd", listOf(
        Rule("R1", "/a", Status(200,"OK"), listOf()),
        Rule("R2", "/b", Status(200,"OK"), listOf()),
    ), WhConf(listOf(), listOf(), listOf(), listOf(), "", mapOf(), ""))
    configureRouting(srv)
}
