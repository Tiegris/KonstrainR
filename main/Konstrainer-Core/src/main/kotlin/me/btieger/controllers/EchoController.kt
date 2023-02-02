package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.echoController() {
    routing {
        get("/") {
            call.respond("got it")
        }
        get("debug") {
            call.respond("nothing to debug")
        }
    }
}