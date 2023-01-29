package me.btieger.domain

import kotlinx.serialization.Serializable
import me.btieger.persistance.tables.Dsl

@Serializable
data class IdDto(val id: Int)
fun Int.toIdDto() = IdDto(this)

@Serializable
data class DslConciseDto(val id: Int, val name: String, val status: String)
fun Dsl.toConciseDto() = DslConciseDto(id.value, name, status.name)

@Serializable
data class DslDetailedDto(
    val id: Int,
    val name: String,
    val status: String,
    val error: String?,
    val buildStartTimestamp: String?
)

fun Dsl.toDetailedDto() = DslDetailedDto(id.value, name, status.name, errorMessage, buildSubmissionTime?.toString())

@Serializable
data class ServerDto(val id: Int, val dslId: Int)

@Serializable
data class CreateServerDto(val dslId: Int)


