package me.btieger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.util.pipeline.*
import me.btieger.persistance.tables.Role
import me.btieger.services.MyPrincipal
import org.jetbrains.exposed.sql.statements.api.ExposedBlob


inline val ApplicationCall.principal: MyPrincipal?
    get() = this.principal<MyPrincipal>()

inline val ApplicationCall.id: Int
    get() = this.parameters.getOrFail<Int>("id").toInt()

inline val ApplicationCall.contentType: ContentType
    get() = this.request.contentType()

inline val PipelineContext<*, ApplicationCall>.logger: Logger
    get() = this.application.environment.log

@KtorDsl
suspend inline fun PipelineContext<*, ApplicationCall>.exceptionGuard(block: () -> Unit) =
    try {
        block()
    } catch (e: Throwable) {
        e.message?.let {
            logger.error(it)
            internalServerError(it)
        }
        logger.error(e.stackTraceToString())
    }

@KtorDsl
suspend inline fun PipelineContext<*, ApplicationCall>.inRole(role: Role, block: () -> Unit) {
    context.principal?.let {
        if (it.role == Role.Admin || it.role == role) {
            block()
        } else {
            return forbidden()
        }
    }
}


val ExposedBlob.bytesStable: ByteArray
    get() = inputStream.readBytes()


