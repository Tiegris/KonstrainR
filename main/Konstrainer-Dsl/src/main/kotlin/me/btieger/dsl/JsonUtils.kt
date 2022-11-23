package me.btieger.dsl

import kotlinx.serialization.json.*

private val _regex = "/(?=(?:[^\']*\'[^\']*\')*[^\']*\$)".toRegex()

@DslMarkerBlock
infix fun JsonObject.jqx(s: String): JsonElement? {
    val s = s.removePrefix("/").removeSuffix("/")
    val splits = s.split(_regex)
    var o : JsonElement? = this
    for (getter in splits) {
        val getterInt = getter.toIntOrNull()
        o = if (getterInt == null) {
            o?.jsonObject?.get(getter)
        } else {
            o?.jsonArray?.get(getterInt)
        }
        o ?: return null
    }
    return o
}

@DslMarkerBlock object int
@DslMarkerBlock object bool
@DslMarkerBlock object double
@DslMarkerBlock object string
@DslMarkerBlock infix fun JsonElement?.parseAs(type :int): Int? = this?.jsonPrimitive?.content?.toIntOrNull()
@DslMarkerBlock infix fun JsonElement?.parseAs(type :bool): Boolean? = this?.jsonPrimitive?.content?.toBooleanStrictOrNull()
@DslMarkerBlock infix fun JsonElement?.parseAs(type: double): Double? = this?.jsonPrimitive?.content?.toDoubleOrNull()
@DslMarkerBlock infix fun JsonElement?.parseAs(type :string): String? = this?.jsonPrimitive?.content
