package me.btieger.dsl

import kotlinx.serialization.json.JsonObject

@DslMarkerBlock
fun server(setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder()
    builder.setup()
    return builder.build()
}

class ServerBuilder {
    private var _rules: List<Rule> by setOnce()
    private var _whconf: WhConf by setOnce()

    @DslMarkerBlock
    var whName: String by setOnce()

    @DslMarkerBlock
    var serverBaseImage: String by setOnce()

    @DslMarkerBlock
    fun whconf(setup: WhConfBuilder.() -> Unit) {
        val builder = WhConfBuilder()
        builder.setup()
        _whconf = builder.build()
    }

    @DslMarkerBlock
    fun rules(setup: RulesBuilder.() -> Unit) {
        val builder = RulesBuilder()
        builder.setup()
        _rules = builder.build()
    }

    internal fun build(): Server {
        // assert no null
        return Server(whName, serverBaseImage, _rules, _whconf)
    }
}

class Server(val whName: String, val serverBaseImage: String, val rules: List<Rule>, val whConf: WhConf)
