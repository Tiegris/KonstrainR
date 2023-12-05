package me.btieger.dsl

import kotlinx.serialization.json.*

private val _regex = "/(?=(?:[^\']*\'[^\']*\')*[^\']*\$)".toRegex()

@DslMarkerBlock
infix fun JsonElement.jqx(s: String): JsonElement {
    try {
        val s = s.removePrefix("/").removeSuffix("/")
        val splits = s.split(_regex)
        var o: JsonElement = this
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
    } catch (e: Throwable) {
        return JsonNull
    }

}

@DslMarkerConstant
val JsonEmpty = buildJsonObject { }

@DslMarkerBlock
object int
@DslMarkerBlock
object bool
@DslMarkerBlock
object double
@DslMarkerBlock
object string

fun <T> JsonElement.nullGuard(fn: JsonElement.() -> T): T? {
    if (this is JsonNull)
        return null
    return if (this is JsonPrimitive)
        fn()
    else
        null
}

@DslMarkerBlock
infix fun JsonElement.parseAs(type: int): Int? = nullGuard { this.jsonPrimitive.content.toIntOrNull() }
@DslMarkerBlock
infix fun JsonElement.parseAs(type: bool): Boolean? = nullGuard { this.jsonPrimitive.content.toBooleanStrictOrNull() }
@DslMarkerBlock
infix fun JsonElement.parseAs(type: double): Double? = nullGuard { this.jsonPrimitive.content.toDoubleOrNull() }
@DslMarkerBlock
infix fun JsonElement.parseAs(type: string): String? = nullGuard { this.jsonPrimitive.content }


