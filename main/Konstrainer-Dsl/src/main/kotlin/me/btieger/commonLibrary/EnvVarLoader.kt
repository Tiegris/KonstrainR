package me.btieger.commonLibrary

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

open class EnvVarSettings(private val prefix: String) {
    private var getEnv: (String) -> String? = System::getenv

    private val envVarMocks: MutableMap<String, String> = mutableMapOf()
    private fun interceptedGetEnv(key: String): String? {
        if (key in envVarMocks)
            return envVarMocks[key]
        return System.getenv(key)
    }

    fun mockEnvVar(key: String, value: String) {
        getEnv = ::interceptedGetEnv
        envVarMocks[key] = value
    }

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
                cache = getEnv(_key)?.transform() ?: default
            }
            return cache ?: throw Exception("Env var: `${_key}` not set!")
        }
    }
}