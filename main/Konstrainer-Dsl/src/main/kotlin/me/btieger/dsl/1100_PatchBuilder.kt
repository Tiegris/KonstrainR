package me.btieger.dsl

import kotlinx.serialization.json.*

class PatchBuilder {
    private val _patches = mutableListOf<JsonObject.() -> JsonObject>()

    @DslMarkerBlock
    fun add(path: String, value: String): (JsonObject.() -> JsonArray) {
        _patches += fun JsonObject.(): JsonObject = buildJsonObject {
            this.put("op", "add")
            put("path", path)
            put("value", value)
        }

        return fun JsonObject.(): JsonArray {
            val input = this
            return buildJsonArray {
                _patches.forEach {
                    add(input.it())
                }
            }
        }
    }

//    @DslMarkerBlock
//    fun remove(path: String) {
//        _patches += mapOf(
//            "op" to "remove",
//            "path" to path,
//        )
//    }
//
//    @DslMarkerBlock
//    fun replace(path: String, value: String) {
//        _patches += mapOf(
//            "op" to "replace",
//            "path" to path,
//            "value" to value,
//        )
//    }
//
//    @DslMarkerBlock
//    fun copy(from: String, to: String) {
//        _patches += mapOf(
//            "op" to "copy",
//            "from" to from,
//            "path" to to,
//        )
//    }
//
//    @DslMarkerBlock
//    fun move(from: String, to: String) {
//        _patches += mapOf(
//            "op" to "move",
//            "from" to from,
//            "path" to to,
//        )
//    }
//
//    @DslMarkerBlock
//    fun test(path: String, value: String) {
//        _patches += mapOf(
//            "op" to "add",
//            "path" to path,
//            "value" to value,
//        )
//    }

}
