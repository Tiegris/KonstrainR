package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class WatchProvider(val kubectl: KubernetesClient)
typealias WatchFunction<T> = WatchProvider.() -> KubernetesResourceList<T>
typealias MarkCondition = () -> Boolean
class MarkerObject(val status: String, val condition: MarkCondition)
class WatchBehaviorProvider<T>(val item: T) {
    internal val markers = mutableListOf<MarkerObject>()

    @DslMarkerBlock
    fun mark(status: String, condition: MarkCondition) {
        markers += MarkerObject( status, condition)
    }
}
typealias WatchBehaviorFunction<T> = WatchBehaviorProvider<T>.() -> Unit

@Serializable
class Mark(val fullResourceName: String, val name: String, val namespace: String, val marks: List<String>)

class Monitor<T : HasMetadata>(val monitorName: String, private val watch: WatchFunction<T>, private val behavior: WatchBehaviorFunction<T>) {
    fun evaluate(kubectl: KubernetesClient): List<Mark> {
        val resources = WatchProvider(kubectl).run(watch)

        val result = mutableListOf<Mark>()

        resources.items.forEach { resource ->
            val provider = WatchBehaviorProvider(resource).apply(behavior)
            val marks = mutableListOf<String>()
            provider.markers.forEach { b ->
                if (b.condition()) {
                    marks += b.status
                }
            }
            if (marks.isNotEmpty()) {
                result += Mark(resource.fullResourceName, resource.metadata.name, resource.metadata.namespace, marks)
            }


        }
        return result
    }
}






abstract class StatusByColor(val string: String)
@DslMarkerConstant object YELLOW : StatusByColor("yellow")
@DslMarkerConstant object GREEN : StatusByColor("green")
@DslMarkerConstant object RED : StatusByColor("red")
