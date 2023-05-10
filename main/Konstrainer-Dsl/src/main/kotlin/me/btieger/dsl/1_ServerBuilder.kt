package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata

@DslMarkerBlock
fun server(uniqueName: String, permissions: Permissions? = null, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder(permissions).apply(setup)
    return builder.build(uniqueName)
}

class ServerBuilder(permissions: Permissions?) {
    private var _webhooks: MutableList<Webhook> = mutableListOf()
    private var _monitors: MutableList<Monitor<out HasMetadata>> = mutableListOf()
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
    fun <T : HasMetadata> monitor(monitorName: String, watch: WatchFunction<T>, behavior: WatchBehaviorFunction<T>) {
        _monitors += Monitor(monitorName, watch, behavior)
    }

    internal fun build(name: String): Server {
        return Server(name, _webhooks, _monitors, _permission)
    }
}

class Server(
    val name: String,
    val webhooks: List<Webhook>,
    val monitors: List<Monitor<out HasMetadata>>,
    val permissions: Permissions?,
)
