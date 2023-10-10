package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient

@DslMarkerBlock
fun server(uniqueName: String, setup: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder().apply(setup)
    return builder.build(uniqueName)
}

typealias ClusterRoleName = String

@DslMarkerConstant
val ReadAny = "ReadAny"

class ServerBuilder {
    private var _webhooks: MutableList<Webhook> = mutableListOf()
    private var _monitors: MutableList<Monitor<out HasMetadata>> = mutableListOf()
    private var _complexMonitor: CustomMonitorBehaviorFunction? by setMaxOnce()

    @DslMarkerVerb5
    var clusterRole by setMaxOnce<ClusterRoleName>()

    @DslMarkerBlock
    fun webhook(uniqueName: String, defaults: WebhookConfigBundle? = null, setup: WebhookBuilder.() -> Unit) {
        val builder = WebhookBuilder(defaults ?: WebhookConfigBundleBuilder().build()).apply(setup)
        _webhooks += builder.build(uniqueName)
    }

    @DslMarkerBlock
    fun report(behavior: CustomMonitorBehaviorFunction) {
        _complexMonitor = behavior
    }

    internal fun build(name: String): Server {
        return Server(name, _webhooks, clusterRole, _complexMonitor)
    }
}

class MonitorResponse(val results: MutableMap<String, MutableList<Tag>>, val errors: List<String>)
class Server(
    val name: String,
    val webhooks: List<Webhook>,
    val clusterRole: ClusterRoleName?,
    private val report: CustomMonitorBehaviorFunction?
) {

    fun hasMonitors(): Boolean {
        return report != null
    }

    fun evaluateMonitors(k8s: KubernetesClient): MonitorResponse {
        val results = mutableMapOf<String, MutableList<Tag>>()

        var errors: List<String>? = null
        report?.let {
            val monitor = CustomMonitorBehaviorProvider(k8s).apply(it)
            val aggregations = monitor.getAggregations()
            errors = monitor.getErrors()
            aggregations.forEach {
                if (results[it.name] == null)
                    results[it.name] = mutableListOf()
                results[it.name]!!.addAll(it.collection)
            }
        }

        return MonitorResponse(results, errors ?: listOf())
    }

}
