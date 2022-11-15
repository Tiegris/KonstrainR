package me.btieger.domain

import kotlinx.serialization.Serializable
import me.btieger.persistance.tables.*

@Serializable
data class DslDto(val id: Int, val name: String)
inline val Dsl.dto
    get() = DslDto(id.value, name)

@Serializable
data class ServerDto(val id: Int, val dslId: Int)
inline val Server.dto
    get() = ServerDto(id.value, dsl.id.value)

@Serializable
data class CreateServerDto(val dslId: Int)