package btieger.me

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import btieger.me.plugins.*
import btieger.me.plugins.controllers.controllers
import btieger.me.plugins.web.configureCSS
import btieger.me.plugins.web.configureRouting
import io.ktor.server.webjars.*


fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Webjars) {
        path = "assets"
    }
    configureSecurity()
    configureSerialization()
    controllers()
    configureCSS()
    configureRouting()
    //configureSockets()
}
