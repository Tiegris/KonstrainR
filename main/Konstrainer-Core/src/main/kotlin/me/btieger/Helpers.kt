package me.btieger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable

inline val ApplicationCall.id: Int
    get() = this.parameters.getOrFail<Int>("id").toInt()

inline val ApplicationCall.contentType: ContentType
    get() = this.request.contentType()
