package btieger.me.plugins.controllers

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.controllers() {
    routing {
        route("/api/v1/") {
            post("dsl-upload") {
                call.respondRedirect("/servers")
            }
        }
    }
}