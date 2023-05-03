package me.btieger.dsl

import kotlinx.serialization.json.JsonArray

class RuleInstance(val allowed: Boolean, val patch: JsonArray, val warnings: List<Warning>, val status: Status)
class RuleBehaviorBuilder {
    private var _allowed: Boolean by setOnce(true)
    private var _patch: JsonArray by setOnce()
    private val _warnings = mutableListOf<Warning>()
    private var _status: Status? = null

    @DslMarkerBlock
    fun allowed(script: () -> Boolean) {
        _allowed = script()
    }

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        val builder = PatchBuilder()
        builder.setup()
        _patch = builder.build()
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder()
        builder.setup()
        TODO()
    }


    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder("")
        builder.setup()
        if (_status != null)
            throw MultipleSetException("Status can only be set once!")
        _status = builder.build()
    }

    fun build() = RuleInstance(_allowed,_patch,_warnings, _status!!)

}

class Warning
class WarningsBuilder

class Status(val code: Int, val message: String)
class StatusBuilder(ruleName: String) {

    @DslMarkerVerb5
    var code: Int by setOnce(403)
    @DslMarkerVerb5
    var message: String by setOnce("Denied by rule: $ruleName")

    internal fun build() = Status(code, message)

}