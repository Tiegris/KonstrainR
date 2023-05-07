package me.btieger.dsl

@DslMarkerBlock
fun permissions(setup: PermissionsBuilder.() -> Unit): Permissions {
    val builder = PermissionsBuilder().apply(setup)
    return builder.build()
}

typealias Permissions = List<Rule>
class PermissionsBuilder {
    private val _rules = mutableListOf<Rule>()

    @DslMarkerBlock
    fun rule(setup: RuleBuilder.() -> Unit) {
        val builder = RuleBuilder().apply(setup)
        _rules += builder.build()
    }

    internal fun build(): Permissions {
        return _rules
    }
}

class Rule(val apiGroups: List<String>, val resources: List<String>, val verbs: List<String>)
class RuleBuilder {
    private var _apiGroups: List<String> by setExactlyOnce()
    private var _resources: List<String> by setExactlyOnce()
    private var _verbs: List<String> by setExactlyOnce()

    @DslMarkerVerb5
    fun apiGroups(vararg apiGroups: String) {
        _apiGroups = apiGroups.toList()
    }

    @DslMarkerVerb5
    fun resources(vararg resources: String) {
        _resources = resources.toList()
    }

    @DslMarkerVerb5
    fun verbs(vararg verbs: String) {
        _verbs = verbs.toList()
    }

    internal fun build(): Rule {
        return Rule(_apiGroups, _resources, _verbs)
    }
}