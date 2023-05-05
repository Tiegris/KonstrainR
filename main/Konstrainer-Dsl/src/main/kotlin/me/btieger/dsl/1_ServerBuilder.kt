package me.btieger.dsl

@DslMarkerBlock
fun server(uniqueName: String, permissions: Permissions? = null, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder(permissions).apply(setup)
    return builder.build(uniqueName)
}

class ServerBuilder(permissions: Permissions?) {
    private var _webhooks: List<Webhook> = mutableListOf()
    private var _aggregations: List<Aggregation> = mutableListOf()
    private var _permission: Permissions? by setMaxOnce(permissions)

    @DslMarkerBlock
    fun webhook(uniqueName: String, defaults: WebhookConfigBundle? = null, setup: WebhookBuilder.() -> Unit) {
        val builder = WebhookBuilder(defaults ?: WebhookConfigBundleBuilder().build()).apply(setup)
        _webhooks += builder.build(uniqueName)
    }

    @DslMarkerBlock
    fun permissions(setup: PermissionsBuilder.() -> Unit) {
        val builder = PermissionsBuilder().apply(setup)
        _permission = builder.build()
    }

    @DslMarkerBlock
    fun aggregation(name: String, runner: AggregationRunnerFunction) {
        _aggregations += Aggregation(name, runner)
    }

    internal fun build(name: String): Server {
        return Server(name, _webhooks, _aggregations, _permission)
    }
}

class Server(val name: String, val webhooks: List<Webhook>, val aggregations: List<Aggregation>, val permissions: Permissions?)
