package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClient

typealias AggregationRunnerFunction = AggregationRunner.() -> Unit

class Aggregation(val name: String, val runner: AggregationRunnerFunction)

class AggregationRunner(val kubectl: KubernetesClient) {
    val markings = mutableListOf<Marking>()

    @DslMarkerBlock
    fun <T: HasMetadata> forEach(resource: KubernetesResourceList<T>, function: T.()->Unit) {
        resource.items.forEach { it.function() }
    }

    @DslMarkerBlock
    fun HasMetadata.mark(status: StatusByColor, comment: String? = null) {
        markings += Marking("${fullResourceName}/${metadata.name}", status, comment, metadata.namespace)
    }

}

class Marking(val named: String, val status: StatusByColor, val comment: String?, val namespace: String?)

abstract class StatusByColor(val string: String)
object YELLOW : StatusByColor("yellow")
object GREEN : StatusByColor("green")
object RED : StatusByColor("red")