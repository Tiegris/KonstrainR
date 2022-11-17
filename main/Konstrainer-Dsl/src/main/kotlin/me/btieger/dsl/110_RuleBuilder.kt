package me.btieger.dsl

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

open class RuleBuilder {
    @DslMarkerVerb5
    var name: String by setOnce()
    @DslMarkerVerb5
    var path: String by setOnce()

    @DslMarkerVerb5
    var behaviour: (JsonObject) -> RuleInstance by setOnce()

    internal open fun build(): Rule {
        name = validateName(name)
        path = validatePath(path)

        return Rule(name, path, behaviour)
    }

    private fun validateName(name: String): String {
        val name = name.split(' ', '.').joinToString(separator = "-")
        for (c in path) {
            if (!(c in 'A'..'Z' || c in 'a'..'z' || c == '-' || c == '_'))
                throw Exception()
        }
        return name
    }

    private fun validatePath(path: String): String {
        var path = path
        if (path.first() != '/')
            path = "/$path"
        path.trimEnd('/')
        for (c in path) {
            if (c !in 'a'..'z' && c != '/')
                throw Exception()
        }
        return path
    }
}

class Rule(val name: String, val path: String, val provider: (JsonObject) -> RuleInstance)

class RuleInstance(val allowed: Boolean, val patch: JsonArray, val warnings: List<Warning>, val status: Status)
class RuleProvider() {
    private var _allowed: Boolean by setOnce(true)
    private var _patch: JsonArray by setOnce()
    private val _warnings = mutableListOf<Warning>()
    private var _status: Status? = null

    @DslMarkerBlock
    fun allowed(script: () -> Boolean) {
        _allowed = script()
    }

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        val builder = PatchBuilder()
        builder.setup()
        builder.build()
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder()
        builder.setup()
    }


    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder("")
        builder.setup()
        if (_status != null)
            throw MultipleSetException("Status can only be set once!")
        _status = builder.build()
    }

    fun build() = RuleInstance(_allowed,_patch,_warnings, _status!!)


}

class Warning
class WarningsBuilder

class Status(val code: Int, val message: String)
class StatusBuilder(ruleName: String) {

    @DslMarkerVerb5
    var code: Int by setOnce(403)
    @DslMarkerVerb5
    var message: String by setOnce("Denied by rule: $ruleName")

    internal fun build() = Status(code, message)

}