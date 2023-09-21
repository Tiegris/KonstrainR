package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.serialization.Serializable

class WatchProvider(val kubectl: KubernetesClient)
typealias WatchFunction<T> = WatchProvider.() -> KubernetesResourceList<T>
typealias MarkCondition = () -> Boolean
class MarkerObject(val status: String, val condition: MarkCondition)
class WatchBehaviorProvider<T>(val item: T) {
    internal val markers = mutableListOf<MarkerObject>()

    @DslMarkerBlock
    fun tag(status: String, condition: MarkCondition) {
        markers += MarkerObject( status, condition)
    }
}
typealias WatchBehaviorFunction<T> = WatchBehaviorProvider<T>.() -> Unit

@Serializable
class Tag(val fullResourceName: String, val name: String, val namespace: String?, val marks: List<String>) {
    constructor(meta: TagMeta, marks: List<String>) : this(meta.fullResourceName, meta.name, meta.namespace, marks)
}
class TagMeta(val fullResourceName: String, val name: String, val namespace: String?) {
    constructor(resource: HasMetadata) : this(resource.fullResourceName, resource.metadata.name, resource.metadata.namespace)
}

class Monitor<T : HasMetadata>(val monitorName: String, private val watch: WatchFunction<T>, private val behavior: WatchBehaviorFunction<T>) {
    fun evaluate(kubectl: KubernetesClient): List<Tag> {
        val resources = WatchProvider(kubectl).run(watch)

        val tags = mutableListOf<Tag>()

        resources.items.forEach { resource ->
            val provider = WatchBehaviorProvider(resource).apply(behavior)
            val marks = mutableListOf<String>()
            provider.markers.forEach { b ->
                if (b.condition()) {
                    marks += b.status
                }
            }
            if (marks.isNotEmpty()) {
                tags += Tag(resource.fullResourceName, resource.metadata.name, resource.metadata.namespace, marks)
            }


        }
        return tags
    }
}

