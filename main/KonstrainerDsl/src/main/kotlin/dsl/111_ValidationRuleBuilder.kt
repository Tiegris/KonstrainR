package dsl

class ValidationRuleBuilder {


    internal fun build(): ValidationRule {
        return ValidationRule()
    }
}

class ValidationRule : Rule