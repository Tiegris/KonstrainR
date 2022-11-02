package dsl


@Suppress("ClassName")
@DslMarkerBlock
object mutating

@Suppress("ClassName")
@DslMarkerBlock
object validation

class RulesBuilder {

    val _rules = mutableListOf<Rule>()

    @DslMarkerBlock
    infix fun mutating.rule(setup: MutatingRuleBuilder.() -> Unit) {
        val builder = MutatingRuleBuilder()
        builder.setup()
        _rules += builder.build()
    }

    @DslMarkerBlock
    infix fun validation.rule(setup: MutatingRuleBuilder.() -> Unit) {
        val builder = MutatingRuleBuilder()
        builder.setup()
        _rules += builder.build()
    }

    internal fun build(): Rules {
        return Rules()
    }

}

class Rules
interface Rule