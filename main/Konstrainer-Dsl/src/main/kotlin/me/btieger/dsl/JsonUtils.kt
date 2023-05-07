package me.btieger.dsl

import kotlinx.serialization.json.*

private val _regex = "/(?=(?:[^\']*\'[^\']*\')*[^\']*\$)".toRegex()

@DslMarkerBlock
infix fun JsonObject.jqx(s: String): JsonElement {
    try {
        val s = s.removePrefix("/").removeSuffix("/")
        val splits = s.split(_regex)
        var o : JsonElement = this
        for (getter in splits) {
            val getterInt = getter.toIntOrNull()
            o = if (getterInt == null) {
                o.jsonObject[getter] ?: JsonNull
            } else {
                o.jsonArray[getterInt]
            }
            if (o is JsonNull) return JsonNull
        }
        return o
    } catch(e: Throwable) {
        return JsonNull
    }

}

@DslMarkerBlock object int
@DslMarkerBlock object bool
@DslMarkerBlock object double
@DslMarkerBlock object string
fun <T> JsonElement.guard(fn: JsonElement.()->T) : T? {
    return if (this is JsonPrimitive)
        fn()
    else
        null
}
@DslMarkerBlock infix fun JsonElement.parseAs(type :int): Int? = guard { this.jsonPrimitive.content.toIntOrNull() }
@DslMarkerBlock infix fun JsonElement.parseAs(type :bool): Boolean? = guard { this.jsonPrimitive.content.toBooleanStrictOrNull() }
@DslMarkerBlock infix fun JsonElement.parseAs(type: double): Double? = guard { this.jsonPrimitive.content.toDoubleOrNull() }
@DslMarkerBlock infix fun JsonElement.parseAs(type :string): String? = guard { this.jsonPrimitive.content }
