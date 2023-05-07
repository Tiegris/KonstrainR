package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata

@DslMarkerBlock
fun server(uniqueName: String, permissions: Permissions? = null, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder(permissions).apply(setup)
    return builder.build(uniqueName)
}

class ServerBuilder(permissions: Permissions?) {
    private var _webhooks: MutableList<Webhook> = mutableListOf()
    private var _aggregations: MutableList<Aggregation> = mutableListOf()
    private var _watches: MutableMap<String, WatchRunnerFunction> = mutableMapOf()
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

    @DslMarkerBlock
    fun watch(name: String, runner: WatchRunnerFunction) {
        _watches[name] = runner
    }

    internal fun build(name: String): Server {
        return Server(name, _webhooks, _aggregations, _permission, _watches)
    }
}

class Server(
    val name: String,
    val webhooks: List<Webhook>,
    val aggregations: List<Aggregation>,
    val permissions: Permissions?,
    val watches: MutableMap<String, WatchRunnerFunction>
)
