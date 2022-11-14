package me.btieger.dsl

class NamespaceSelectorBuilder {

    private var _selctorRule: MatchLabel by setOnce()

    @DslMarkerBlock
    fun matchLabels(setup: MatchLabelBuilder.() -> Unit) {
        val builder = MatchLabelBuilder()
        builder.setup()
        _selctorRule = builder.build()
    }

    internal fun build(): NamespaceSelector {
        return NamespaceSelector(_selctorRule)
    }
}

class NamespaceSelector(val selectorRule: MatchLabel)

class MatchLabelBuilder {

    val _rules = mutableMapOf<String, String>()

    @DslMarkerVerb5
    infix fun String.eq(value: String) {
        _rules[this] = value
    }

    internal fun build(): MatchLabel {
        return MatchLabel(_rules)
    }
}

class MatchLabel(val rules: Map<String, String>)