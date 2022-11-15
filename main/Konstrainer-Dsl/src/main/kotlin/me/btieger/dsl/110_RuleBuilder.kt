package me.btieger.dsl

import kotlinx.serialization.json.JsonObject

open class RuleBuilder {
    @DslMarkerVerb5
    var name: String by setOnce()
    @DslMarkerVerb5
    var path: String by setOnce()

    private val _warnings = mutableListOf<Warning>()
    private var _status: Status? = null

    private var _allowed: ((JsonObject) -> Boolean) by setOnce { true }

    @DslMarkerBlock
    fun allowed(script: (JsonObject) -> Boolean) {
        _allowed = script
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder()
        builder.setup()
    }

    fun patch(setup: PatchBuilder.() -> Unit) {

    }

    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder(validateName(name))
        builder.setup()
        if (_status != null)
            throw MultipleSetException("Status can only be set once!")
        _status = builder.build()
    }

    internal open fun build(): Rule {
        name = validateName(name)
        path = validatePath(path)



        return Rule(name, path,
            _status ?: StatusBuilder(name).build(),
            _warnings
        )
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

class Rule(val name: String, val path: String, val status: Status, warnings: List<Warning>)

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