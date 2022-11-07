package me.btieger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*

@DslMarker
annotation class ResponseDslMarker

@ResponseDslMarker
private suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.notFound(data: T) {
    call.respond(HttpStatusCode.NotFound, data)
}
@ResponseDslMarker
suspend inline fun PipelineContext<*, ApplicationCall>.notFound() {
    call.respond(HttpStatusCode.NotFound)
}

@ResponseDslMarker
suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.ok(data: T) {
    call.respond(HttpStatusCode.OK, data)
}
@ResponseDslMarker
suspend inline fun PipelineContext<*, ApplicationCall>.ok() {
    call.respond(HttpStatusCode.OK)
}

@ResponseDslMarker
suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.created(data: T) {
    call.respond(HttpStatusCode.Created, data)
}
@ResponseDslMarker
suspend inline fun PipelineContext<*, ApplicationCall>.created() {
    call.respond(HttpStatusCode.Created)
}

@ResponseDslMarker
suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.badRequest(data: T) {
    call.respond(HttpStatusCode.BadRequest, data)
}
@ResponseDslMarker
suspend inline fun PipelineContext<*, ApplicationCall>.badRequest() {
    call.respond(HttpStatusCode.BadRequest)
}

@ResponseDslMarker
suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.internalServerError(data: T) {
    call.respond(HttpStatusCode.InternalServerError, data)
}
@ResponseDslMarker
suspend inline fun PipelineContext<*, ApplicationCall>.internalServerError() {
    call.respond(HttpStatusCode.InternalServerError)
}
