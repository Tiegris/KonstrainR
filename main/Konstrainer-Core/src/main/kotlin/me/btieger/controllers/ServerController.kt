package me.btieger.controllers

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import me.btieger.domain.CreateServerDto
import me.btieger.id
import me.btieger.notFound
import me.btieger.ok
import me.btieger.persistance.tables.Server


fun Application.serverController() {
//    routing {
//        route("/server") {
//            get {
//                val res = serverService.all()
//                ok(res)
//            }
//            get("/{id}") {
//                val id = call.id
//                val res = serverService.read(id, Server::) ?: return@get notFound()
//                ok(res)
//            }
//            post {
//                val body = call.receive<CreateServerDto>()
//                val result = serverService.create(body)
//
//                result?.let{
//                    ok(it)
//                } ?: notFound()
//            }
//            delete("/{id}") {
//                val id = call.id
//                val result = serverService.delete(id)
//                if (result)
//                    ok()
//                else
//                    notFound()
//            }
//        }
//    }

}
