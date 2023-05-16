package me.btieger.dsl

class WarningsBuilder {
    private val _warnings: MutableList<String> = mutableListOf()

    @DslMarkerVerb5
    fun warning(warning: String) {
        _warnings += warning
    }

    internal fun build(): MutableList<String>? {
        return if (_warnings.isNotEmpty())
            _warnings
        else null
    }
}
