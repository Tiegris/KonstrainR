package me.btieger.controllers

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import me.btieger.*
import me.btieger.persistance.tables.Dsl
import me.btieger.domain.dto
import me.btieger.persistance.services.dslService

fun Application.dslController() {
    routing {
        route("/dsl") {
            get {
                val res = dslService.all()
                ok(res)
            }
            get("/{id}") {
                val id = call.id
                val res = dslService.read(id, Dsl::dto) ?: notFound()
                ok(res)
            }
            get("/{id}/file") {
                val id = call.id
                val file = dslService.read(id) { String(file.bytes) } ?: return@get notFound()
                ok(file)
            }
            post {
                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val filename = part.originalFileName
                                ?: return@forEachPart badRequest( "No file name")
                            val fileBytes = part.streamProvider().readBytes()
                            val result = dslService.create(filename, fileBytes)
                                ?: return@forEachPart internalServerError("Could not upload file")
                            created(result)
                        }
                        else -> {}
                    }
                }
            }
            put("/{id}")  {
                val id = call.id
                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val filename = part.originalFileName
                                ?: return@forEachPart badRequest( "No file name")
                            val fileBytes = part.streamProvider().readBytes()
                            val result = dslService.update(id, filename, fileBytes)
                            if (result)
                                created(id.dto)
                            else
                                internalServerError("Could not upload file")
                        }
                        else -> {}
                    }
                }
            }
            delete("/{id}")  {
                val id = call.id
                val result = dslService.delete(id)
                if (result)
                    ok()
                else
                    notFound()
            }
        }

    }
}
