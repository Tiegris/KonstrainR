package me.btieger.dsl

@Suppress("ClassName")
@DslMarkerBlock
object mutating

@Suppress("ClassName")
@DslMarkerBlock
object validation

class RulesBuilder {

    private val _rules = mutableListOf<Rule>()

    @DslMarkerBlock
    infix fun mutating.rule(setup: MutatingRuleBuilder.() -> Unit) {
        val builder = MutatingRuleBuilder()
        builder.setup()
        _rules += builder.build()
    }

    internal fun build(): List<Rule> {
        return _rules
    }

}

interface Rule