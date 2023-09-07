package me.btieger.controllers

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import me.btieger.*
import me.btieger.services.DslService
import org.koin.ktor.ext.inject

const val apiVersion = "v1"
fun Application.dslController() {
    val dslService by inject<DslService>()
    routing {
        authenticate {

            route("/api/$apiVersion/dsls") {
                // Return all DLS infos (brief)
                get {
                    exceptionGuard {
                        val res = dslService.all()
                        ok(res)
                    }
                }
                // Return DSL info by id (detailed)
                get("{id}") {
                    exceptionGuard {
                        val id = call.id
                        val res = dslService.getDetails(id) ?: return@get notFound()
                        ok(res)
                    }
                }
                // Return textual DSL file
                get("{id}/file") {
                    exceptionGuard {
                        val id = call.id
                        val file = dslService.getFile(id) ?: return@get notFound()
                        ok(file)
                    }
                }
                // Return compiled DSL JAR
                get("{id}/jar") {
                    exceptionGuard {
                        val id = call.id
                        val file = dslService.getJar(id) ?: return@get notReady()
                        ok(file)
                    }
                }
                // Upload compiled DSL JAR
                post("{id}/jar") {
                    exceptionGuard {
                        val id = call.id
                        val secret = call.request.headers["Authorization"] ?: return@post badRequest()
                        val contentType = call.contentType
                        if (contentType.match(ContentType.Text.Plain)) {
                            val errorReport: String = call.receive()
                            dslService.setError(id, secret, errorReport)
                            return@post ok()
                        }
                        if (contentType.match("application/java-archive")) {
                            val jarBytes: ByteArray = call.receive()
                            dslService.setJar(id, secret, jarBytes)
                            return@post ok()
                        }
                        badRequest()
                    }
                }
                // Upload DSL source code
                post {
                    exceptionGuard {
                        val multipartData = call.receiveMultipart()
                        multipartData.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                val filename = part.originalFileName
                                    ?: return@forEachPart badRequest("No file name")
                                val fileBytes = part.streamProvider().readBytes()
                                val result = dslService.createDsl(filename, fileBytes)
                                    ?: return@forEachPart internalServerError("Could not upload file")
                                created(result)
                            }
                        }
                    }
                }
                // Delete DSL entity
                delete("{id}") {
                    exceptionGuard {
                        val id = call.id
                        val result = dslService.deleteDsl(id)
                        if (result)
                            ok()
                        else
                            notFound()
                    }
                }
            }

        }
    }
}
