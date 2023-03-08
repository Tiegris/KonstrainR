package me.btieger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureSerialization
import me.btieger.services.DslService
import me.btieger.services.DslServiceImpl
import org.slf4j.event.Level

class Configuration(val serviceName: String, val builderImage: String, val buildJobTtlMinutes: Int, val namespace: String)
val configuration = Configuration(
    System.getenv("KSR_SERVICE_NAME"),
    System.getenv("KSR_BUILDER_JOB_IMAGE") ?: "tiegris/konstrainer-builder:dev",
    System.getenv("KSR_BUILDER_JOB_TTL_MINUTES")?.toInt() ?: 1,
    System.getenv("KSR_NAMESPACE") ?: "konstrainer-ns",
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }
    configureSerialization()
    echoController()
    dslController()
    //serverController()
    DatabaseFactory.init()
}
