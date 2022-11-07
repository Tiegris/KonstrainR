package me.btieger.controllers

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import me.btieger.dal.dslDao
import me.btieger.dal.tables.Dsl
import me.btieger.dto
import me.btieger.id
import java.io.File

fun Application.dslController() {
    routing {
        route("/dsl") {
            get {
                val res = dslDao.all()
                call.respond(res)
            }
            get("/{id}") {
                val id = call.id
                val res = dslDao.read(id, Dsl::responseDto) ?: return@get call.respond(HttpStatusCode.NotFound, "No dsl with given Id")
                call.respond(res)
            }
            get("/{id}/file") {
                val id = call.id
                val res = dslDao.read(id, Dsl::parse) ?: return@get call.respond(HttpStatusCode.NotFound, "No dsl with given Id")
                val file = String(res.file)
                call.respond(file)
            }
            post {
                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val filename = part.originalFileName
                                ?: return@forEachPart call.respond(HttpStatusCode.BadRequest, "No file name")
                            val fileBytes = part.streamProvider().readBytes()
                            val result = dslDao.create(filename, fileBytes)
                                ?: return@forEachPart call.respond(HttpStatusCode.InternalServerError, "Could not upload file")
                            call.respond(HttpStatusCode.Created, result.dto)
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
                                ?: return@forEachPart call.respond(HttpStatusCode.BadRequest, "No file name")
                            val fileBytes = part.streamProvider().readBytes()
                            val result = dslDao.update(id, filename, fileBytes)
                            if (result)
                                call.respond(HttpStatusCode.OK, id.dto)
                            else
                                call.respond(HttpStatusCode.InternalServerError, "Could not upload file")
                        }
                        else -> {}
                    }
                }
            }
            delete("/{id}")  {
                val id = call.id
                val result = dslDao.delete(id)
                if (result)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

    }
}
