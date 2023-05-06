package me.btieger.dsl

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class WebhookDecision(val allowed: Boolean, val patch: JsonArray?, val warnings: List<String>?, val status: Status)
class WebhookBehaviorBuilder(@DslMarkerConstant val request: JsonObject) {
    private var _allowed: Boolean by setExactlyOnce(true)
    private var _warnings: List<String>? by setMaxOnce()
    private var _patch: JsonArray? by setMaxOnce()
    private var _status: Status by setExactlyOnce(StatusBuilder().build())

    @DslMarkerBlock
    fun allowed(script: () -> Boolean) {
        _allowed = script()
    }

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        val builder = PatchBuilder().apply(setup)
        _patch = builder.build()
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder().apply(setup)
        _warnings = builder.build()
    }

    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder().apply(setup)
        _status = builder.build()
    }

    fun build() = WebhookDecision(_allowed, _patch, _warnings, _status)

}



