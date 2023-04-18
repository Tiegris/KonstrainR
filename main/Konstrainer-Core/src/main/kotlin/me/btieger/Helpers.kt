package me.btieger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

inline val ApplicationCall.id: Int
    get() = this.parameters.getOrFail<Int>("id").toInt()

inline val ApplicationCall.contentType: ContentType
    get() = this.request.contentType()

inline val PipelineContext<*, ApplicationCall>.logger: Logger
    get() = this.application.environment.log

@KtorDsl
suspend inline fun PipelineContext<*, ApplicationCall>.exceptionGuard(block: () -> Unit) =
    try {
        block()
    } catch (e: Throwable) {
        e.message?.let {
            logger.error(it)
            internalServerError(it)
        }
        logger.error(e.stackTraceToString())
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

val ExposedBlob.bytesStable: ByteArray
    get() = inputStream.readBytes()

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

