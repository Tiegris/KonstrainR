package me.btieger

import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable

@Serializable
data class IdDto(val id: Int)

inline val Int.dto: IdDto
    get() = IdDto(this)

inline val ApplicationCall.id: Int
    get() = this.parameters.getOrFail<Int>("id").toInt()
