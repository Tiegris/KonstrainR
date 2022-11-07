package me.btieger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    echoController()
    dslController()
    DatabaseFactory.init()
}
