package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.dslController() {
    routing {
        route("/dsl") {
            get {

            }
            get("/{id}") {

            }
            post {

            }
            put("/{id}")  {

            }
            delete("/{id}")  {

            }
        }

    }
}