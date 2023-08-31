package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata

@DslMarkerBlock
fun server(uniqueName: String, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder().apply(setup)
    return builder.build(uniqueName)
}

typealias ClusterRoleName = String
@DslMarkerConstant
val ReadAny = "ReadAny"
class ServerBuilder() {
    private var _webhooks: MutableList<Webhook> = mutableListOf()
    private var _monitors: MutableList<Monitor<out HasMetadata>> = mutableListOf()

    @DslMarkerVerb5
    var clusterRole by setMaxOnce<ClusterRoleName>()

    @DslMarkerBlock
    fun webhook(uniqueName: String, defaults: WebhookConfigBundle? = null, setup: WebhookBuilder.() -> Unit) {
        val builder = WebhookBuilder(defaults ?: WebhookConfigBundleBuilder().build()).apply(setup)
        _webhooks += builder.build(uniqueName)
    }

    @DslMarkerBlock
    fun <T : HasMetadata> monitor(monitorName: String, watch: WatchFunction<T>, behavior: WatchBehaviorFunction<T>) {
        _monitors += Monitor(monitorName, watch, behavior)
    }

    internal fun build(name: String): Server {
        return Server(name, _webhooks, _monitors, clusterRole)
    }
}

class Server(
    val name: String,
    val webhooks: List<Webhook>,
    val monitors: List<Monitor<out HasMetadata>>,
    val clusterRole: ClusterRoleName?
)
