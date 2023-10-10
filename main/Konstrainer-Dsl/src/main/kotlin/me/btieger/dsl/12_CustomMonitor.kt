package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.AnyNamespaceable
import io.fabric8.kubernetes.client.dsl.Namespaceable
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import io.fabric8.kubernetes.client.dsl.Resource

typealias CustomMonitorBehaviorFunction = CustomMonitorBehaviorProvider.() -> Unit

class TagMetaProvider<T>(_item: T) {
    @DslMarkerConstant
    val item = _item
}
typealias TagMetaProviderFunction<T> = TagMetaProvider<T>.() -> TagMeta

typealias Filter<T> = List<T>.() -> List<T>

class CustomMonitorBehaviorProvider(private val _kubectl: KubernetesClient) {

    private val _aggregations = mutableListOf<Aggregation>()
    private val _errors = mutableListOf<String>()

    fun getAggregations() = _aggregations
    fun getErrors() = _errors

//    @DslMarkerConstant
//    val kubectl = _kubectl

    @DslMarkerBlock
    fun <T> kubectl(
        setup: KubernetesClient.() -> T
    ): T? {
        return try {
            _kubectl.run(setup)
        } catch (e: Exception) {
            _errors += "Error during kubectl call: ${e.message}"
            null
        }
    }

    @DslMarkerBlock
    fun <T : HasMetadata, L : KubernetesResourceList<T>, R : Resource<T>> kubelist(
        omittedNss: List<String> = systemNss, setup: KubernetesClient.() -> NonNamespaceOperation<T, L, R>
    ): List<T> {
        return try {
            val t: NonNamespaceOperation<T, L, R> = _kubectl.run(setup)
            if (t is AnyNamespaceable<*>) {
                omittedNss.forEach { t.withoutField("metadata.namespace", it) }
                (t.inAnyNamespace() as NonNamespaceOperation<T, L, R>).list().items
            } else
                t.list().items
        } catch (e: Exception) {
            _errors += "Error during kubectl call: ${e.message}"
            listOf()
        }
    }

    @DslMarkerBlock
    fun <T : HasMetadata, L : KubernetesResourceList<T>, R : Resource<T>> kubelist(
        namesapce: String,
        setup: KubernetesClient.() -> Namespaceable<NonNamespaceOperation<T, L, R>>
    ): List<T> {
        return try {
            _kubectl.run(setup).inNamespace(namesapce).list().items
        } catch (e: Exception) {
            _errors += "Error during kubectl call: ${e.message}"
            listOf()
        }
    }

    /**
     * kube-system, kube-node-lease, kube-public
     */
    @DslMarkerConstant
    val systemNss = listOf("kube-system", "kube-node-lease", "kube-public")

    /**
     * kube-system, kube-node-lease, kube-public, default
     */
    @DslMarkerConstant
    val nonUserNss = listOf("kube-system", "kube-node-lease", "kube-public", "default")

    @DslMarkerConstant
    val none: List<String> = listOf()

    @DslMarkerBlock
    fun <T> aggregation(
        name: String,
        collection: Iterable<T>,
        tagKey: TagMetaProviderFunction<T> = {
            (item as? HasMetadata)?.let { TagMeta(it) }
                ?: (item as? Map.Entry<*, *>)?.let { (it.key as? HasMetadata)?.let { TagMeta(it) } }
                ?: throw Exception()
        },
        setup: AggregationBuilder<T>.() -> Unit
    ) {
        val tags = mutableListOf<Tag>()
        try {
            for (item in collection) {
                val key = TagMetaProvider(item).run(tagKey)
                val marks = AggregationBuilder(item).apply(setup).markers.filter { it.condition() }.map { it.status }
                if (marks.isNotEmpty())
                    tags += Tag(key, marks)
            }
            _aggregations += Aggregation(name, tags)
        } catch (e: Exception) {
            _errors += "Error when processing aggregation, message: ${e.message}"
        }
    }
}

class AggregationBuilder<T>(_item: T) {
    internal val markers = mutableListOf<MarkerObject>()

    @DslMarkerConstant
    val item = _item

    @DslMarkerBlock
    fun tag(status: String, condition: MarkCondition) {
        markers += MarkerObject(status, condition)
    }

}


class Aggregation(
    val name: String,
    val collection: List<Tag>,
)