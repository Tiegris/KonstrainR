package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import me.btieger.domain.CreateServerDto
import me.btieger.domain.dto
import me.btieger.id
import me.btieger.notFound
import me.btieger.ok
import me.btieger.persistance.services.serverService
import me.btieger.persistance.tables.Server

fun Application.serverController() {
    routing {
        route("/server") {
            get {
                val res = serverService.all()
                ok(res)
            }
            get("/{id}") {
                val id = call.id
                val res = serverService.read(id, Server::dto) ?: return@get notFound()
                ok(res)
            }
            get("/{id}/file") {
                val id = call.id
                val file = serverService.read(id) { jar.bytes } ?: return@get notFound()
                ok(file)
            }
            post {
                val body = call.receive<CreateServerDto>()
                val result = serverService.create(body)
                if (result != null)
                    ok(result)
                else
                    notFound()
            }
            delete("/{id}") {
                val id = call.id

            }
        }
    }

}