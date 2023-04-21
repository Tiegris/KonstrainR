package me.btieger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

@OptIn(ExperimentalSerializationApi::class)
fun JsonArray.toBase64(): String {
    val os = ByteArrayOutputStream()
    Json.encodeToStream(this, os)
    return Base64.getEncoder().encodeToString(os.toByteArray())
}

fun JsonObject.toJsonString(): String {
    return Json.encodeToString(this)
}


open class EnvVarSettings(private val prefix: String) {
    fun loadAll() {
        this::class.memberProperties.forEach {
            it.call(this)
        }
    }

    fun boolean(default: Boolean? = null, key: String? = null): ReadOnlyProperty<Any, Boolean> =
        create(default, key, String::toBooleanStrictOrNull)

    fun int(default: Int? = null, key: String? = null): ReadOnlyProperty<Any, Int> =
        create(default, key, String::toIntOrNull)

    fun long(default: Long? = null, key: String? = null): ReadOnlyProperty<Any, Long> =
        create(default, key, String::toLongOrNull)

    fun string(default: String? = null, key: String? = null): ReadOnlyProperty<Any, String> =
        create(default, key, fun String.() = this)

    private fun <T> create(
        default: T?,
        key: String?,
        transform: String.() -> T?,
    ) = object : ReadOnlyProperty<Any, T> {

        private fun KProperty<*>.key() = key ?: (prefix + name.toScreamingCamelCase())

        private var cache: T? = null
        override fun getValue(thisRef: Any, property: KProperty<*>): T  {
            val _key = property.key()
            if (cache == null) {
                cache = System.getenv(_key)?.transform() ?: default
            }
            return cache ?: throw Exception("Env var: `${_key}` not set!")
        }
    }
}

fun String.toScreamingCamelCase() = StringBuilder().apply {
    for (c in this@toScreamingCamelCase) {
        if (c.isUpperCase()) {
            append('_')
            append(c)
        } else {
            append(c.uppercaseChar())
        }
    }
}.toString()