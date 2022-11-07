package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.echoController() {
    routing {
        get("/") {
            call.respond("got it")
        }
    }
}