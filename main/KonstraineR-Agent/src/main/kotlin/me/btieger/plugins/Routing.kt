package me.btieger.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.JsonObject

import me.btieger.dsl.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        post("/mutate") {
            val x: JsonObject = call.receive()
            println(x)

            val model: Server

            call.respond(HttpStatusCode.OK, "Okkkkkk!")
        }
    }



}
