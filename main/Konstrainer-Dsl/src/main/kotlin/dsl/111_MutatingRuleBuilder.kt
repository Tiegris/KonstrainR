package dsl

class MutatingRuleBuilder : ValidationRuleBuilder() {

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        val builder = PatchBuilder()
        builder.setup()
    }


    override fun build(): Rule {
        return MutatingRule()
    }
}

class ValidationRule : Rule

class PatchBuilder