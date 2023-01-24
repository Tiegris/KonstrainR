package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.btieger.logic.loader.Loader
import java.io.File

fun Application.echoController() {
    routing {
        get("/") {
            call.respond("got it")
        }
        get("debug") {
            val jar = File("C:\\Users\\btieger\\Documents\\KonstrainR\\main\\Konstrainer-Dsl\\build\\libs\\KonstrainerDsl-0.0.1-SNAPSHOT.jar").readBytes()
            val res = Loader.loadServerConfig(jar)
            res.rules
        }
    }
}