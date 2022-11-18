package me.btieger.jsonUtils

import kotlinx.serialization.json.JsonObject

private fun <T> List<T>.tail() = drop(1)

fun JsonObject.read(s: String): Any? {
    val split = s.split('.')
    val ret = if (split.size >= 2) {
        (this[split.first()] as JsonObject).read(split.tail())
    } else {
        this[split.first()]
    }
    return ret
}

private fun JsonObject.read(split: List<String>): Any? {
    val ret = if (split.size >= 2) {
        (this[split.first()] as JsonObject).read(split.tail())
    } else {
        this[split.first()]
    }
    return ret
}