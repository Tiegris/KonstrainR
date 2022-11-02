package dsl

class MutatingRuleBuilder {


    internal fun build(): MutatingRule {
        return MutatingRule()
    }
}

class MutatingRule : Rule