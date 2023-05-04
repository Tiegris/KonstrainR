package me.btieger.dsl

class WarningsBuilder {
    private val _warnings: MutableList<String> = mutableListOf()

    @DslMarkerVerb5
    fun warning(warning: String) {
        _warnings += warning
    }

    internal fun build(): List<String> {
        return _warnings
    }
}
