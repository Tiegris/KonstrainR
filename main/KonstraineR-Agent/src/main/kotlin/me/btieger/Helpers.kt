package me.btieger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
fun JsonArray.toBase64(): String {
    val os = ByteArrayOutputStream()
    Json.encodeToStream(this, os)
    return Base64.getEncoder().encodeToString(os.toByteArray())
}

fun JsonObject.toJsonString(): String {
    return Json.encodeToString(this)
}
