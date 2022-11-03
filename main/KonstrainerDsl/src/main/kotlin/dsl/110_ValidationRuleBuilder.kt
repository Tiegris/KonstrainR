package dsl

import kotlinx.serialization.json.JsonObject

open class ValidationRuleBuilder {
    @DslMarkerVerb5
    var name: String by setOnce()
    @DslMarkerVerb5
    var path: String by setOnce()

    private var _script: ((JsonObject) -> Boolean) by setOnce { true }

    @DslMarkerBlock
    fun allowed(script: (JsonObject) -> Boolean) {
        _script = script
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder()
        builder.setup()
    }

    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder()
        builder.setup()
    }

    internal open fun build(): Rule {
        return ValidationRule()
    }
}

class MutatingRule : Rule


class WarningsBuilder
class StatusBuilder