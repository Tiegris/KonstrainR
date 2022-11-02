package dsl

class MutatingRuleBuilder {
    @DslMarkerVerb5
    var name: String by setOnce()
    @DslMarkerVerb5
    var path: String by setOnce()

    private var _script: ((Map<String, Any>) -> Boolean) by setOnce { true }

    @DslMarkerBlock
    fun allowed(script: (Map<String, Any>) -> Boolean) {
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

    internal fun build(): MutatingRule {
        return MutatingRule()
    }
}

class MutatingRule : Rule


class WarningsBuilder
class StatusBuilder