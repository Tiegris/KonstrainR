package me.btieger.dsl

@DslMarkerBlock
fun server(name: String, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder().apply(setup)
    return builder.build(name)
}

class ServerBuilder {
    private var _components: List<Component> by setOnce()

    @DslMarkerBlock
    fun webhook(name: String, setup: WebhookBuilder.() -> Unit) {
        val builder = WebhookBuilder().apply(setup)
        _components += builder.build(name)
    }

    @DslMarkerBlock
    fun warningAggregator(setup: () -> Unit) {

    }

    internal fun build(name: String): Server {
        // assert no null
        return Server(name, _components)
    }
}

open class Component(val name: String)
class Server(val name: String, val components: List<Component>)
