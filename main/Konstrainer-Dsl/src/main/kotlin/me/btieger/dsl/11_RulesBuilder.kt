package me.btieger.dsl

class RulesBuilder {

    private val _rules = mutableListOf<Rule>()

    @DslMarkerBlock
    fun rule(setup: RuleBuilder.() -> Unit) {
        val builder = RuleBuilder()
        builder.setup()
        _rules += builder.build()
    }

    internal fun build(): List<Rule> {
        return _rules
    }

}
