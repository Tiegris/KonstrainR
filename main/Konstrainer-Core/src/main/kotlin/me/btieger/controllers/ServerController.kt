package me.btieger.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.btieger.*
import me.btieger.services.ServerService
import org.koin.ktor.ext.inject


fun Application.serverController() {
    val serverService by inject<ServerService>()

    routing {
        route("/api/$apiVersion/servers") {
            route("{id}") {
                post("start") {
                    exceptionGuard {
                        try {
                            serverService.start(call.id)
                            respond(HttpStatusCode.Accepted)
                        } catch (e: NotFoundException) {
                            notFound()
                        } catch (e: IllegalStateException) {
                            val message = e.message
                            if (message != null)
                                badRequest(message)
                            else
                                badRequest()
                        }
                    }
                }
                post("stop") {
                    exceptionGuard {
                        serverService.stop(call.id)
                        respond(HttpStatusCode.OK)
                    }
                }
                post("stopall") {
                    exceptionGuard {
                        serverService.stopAll()
                        respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }

}
