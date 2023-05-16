package me.btieger.dsl

import kotlinx.serialization.json.*

class PatchBuilder {
    private val _patches = mutableListOf<JsonObject>()

    fun build() = buildJsonArray {
        _patches.forEach(::add)
    }

    @DslMarkerBlock
    fun add(path: String, value: String) {
        _patches += buildJsonObject {
            put("op", "add")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun add(path: String, value: Number) {
        _patches += buildJsonObject {
            put("op", "add")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun add(path: String, value: JsonElement) {
        _patches += buildJsonObject {
            put("op", "add")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun remove(path: String) {
        _patches += buildJsonObject {
            put("op", "remove")
            put("path", path)
        }
    }

    @DslMarkerBlock
    fun replace(path: String, value: String) {
        _patches += buildJsonObject {
            put("op", "replace")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun replace(path: String, value: Number) {
        _patches += buildJsonObject {
            put("op", "replace")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun replace(path: String, value: JsonElement) {
        _patches += buildJsonObject {
            put("op", "replace")
            put("path", path)
            put("value", value)
        }
    }

    @DslMarkerBlock
    fun copy(from: String, to: String) {
        _patches += buildJsonObject {
            put("op", "copy")
            put("from", from)
            put("path", to)
        }

    }

    @DslMarkerBlock
    fun move(from: String, to: String) {
        _patches += buildJsonObject {
            put("op", "move")
            put("from", from)
            put("path", to)
        }
    }

    @DslMarkerBlock
    fun test(path: String, value: String) {
        _patches += buildJsonObject {
            put("op", "add")
            put("path", path)
            put("value", value)
        }
    }

}
