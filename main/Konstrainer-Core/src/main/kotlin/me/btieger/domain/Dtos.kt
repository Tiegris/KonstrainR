package me.btieger.domain

import kotlinx.serialization.Serializable
import me.btieger.bytesStable
import me.btieger.persistance.tables.Dsl

@Serializable
data class IdDto(val id: Int)
fun Int.toIdDto() = IdDto(this)

@Serializable
data class DslConciseDto(val id: Int, val name: String, val buildStatus: String, val serverStatus: String)
fun Dsl.toConciseDto() = DslConciseDto(id.value, name, buildStatus.name, serverStatus.name)

@Serializable
data class DslDetailedDto(
    val id: Int,
    val name: String,
    val buildStatus: String,
    val error: String?,
    val buildStartTimestamp: String?,
    val serverStatus: String?,
    val hasAggregators: Boolean?,
    val hasWebhooks: Boolean?,
)

fun Dsl.toDetailedDto() =
    DslDetailedDto(id.value, name, buildStatus.name, errorMessage,
        buildSubmissionTime?.toString(),
        serverStatus.name,
        hasAggregators, hasWebhooks
    )

@Serializable
data class DslFullDetailedDto(
    val id: Int,
    val name: String,
    val buildStatus: String,
    val buildError: String?,
    val buildStartTimestamp: String?,
    val serverStatus: String?,
    val file: String,
    val hasAggregators: Boolean?,
    val hasWebhooks: Boolean?,
)

fun Dsl.toFullDetailedDto() =
    DslFullDetailedDto(id.value, name, buildStatus.name, errorMessage,
        buildSubmissionTime?.toString(), serverStatus.name,
        file.bytesStable.decodeToString(),
        hasAggregators, hasWebhooks,
    )

@Serializable
data class ServerDto(val id: Int, val dslId: Int)

@Serializable
data class CreateServerDto(val dslId: Int)


