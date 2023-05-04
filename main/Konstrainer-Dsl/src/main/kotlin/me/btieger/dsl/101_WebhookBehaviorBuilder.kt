package me.btieger.dsl

import kotlinx.serialization.json.JsonArray

class WebhookDecision(val allowed: Boolean, val patch: JsonArray?, val warnings: List<Warning>, val status: Status)
class WebhookBehaviorBuilder {
    private var _allowed: Boolean by setExactlyOnce(true)
    private val _warnings = mutableListOf<Warning>()
    private var _patch: JsonArray? = null
    private var _status: Status by setExactlyOnce(StatusBuilder().build())

    @DslMarkerBlock
    fun allowed(script: () -> Boolean) {
        _allowed = script()
    }

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        if (_patch != null)
            throw MultipleSetException("Patch can only be set once!")
        val builder = PatchBuilder().apply(setup)
        _patch = builder.build()
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder().apply(setup)
        TODO()
    }

    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder().apply(setup)
        _status = builder.build()
    }

    fun build() = WebhookDecision(_allowed, _patch, _warnings, _status)

}



