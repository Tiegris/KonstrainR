package me.btieger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.ByteArrayOutputStream
import java.util.*

private val _serializer = Json.serializersModule.serializer<JsonArray>()
@OptIn(ExperimentalSerializationApi::class)
fun JsonArray.toBase64(): String {
    val os = ByteArrayOutputStream()
    Json.encodeToStream(_serializer, os)
    return Base64.getEncoder().encodeToString(os.toByteArray())
}