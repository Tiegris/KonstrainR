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
    fun <T : HasMetadata, L : KubernetesResourceList<T>, R : Resource<T>> kubectl(
        filter: Filter<T> = {filterOutKubeSystemNss()}, setup: KubernetesClient.() -> NonNamespaceOperation<T, L, R>
    ): List<T> {
        return try {
            val t: NonNamespaceOperation<T, L, R> = _kubectl.run(setup)
                if (t is AnyNamespaceable<*>)
                    (t.inAnyNamespace() as NonNamespaceOperation<T, L, R>).list().items.filter()
                else
                    t.list().items.filter()
        } catch (e: Exception) {
            _errors += "Error during kubectl call: ${e.message}"
            listOf()
        }
    }

    @DslMarkerBlock
    fun <T : HasMetadata, L : KubernetesResourceList<T>, R : Resource<T>> kubectl(
        namesapce: String, filter: Filter<T> = {this}, setup: KubernetesClient.() -> Namespaceable<NonNamespaceOperation<T, L, R>>
    ): List<T> {
        return try {
            _kubectl.run(setup).inNamespace(namesapce).list().items.filter()
        } catch (e: Exception) {
            _errors += "Error during kubectl call: ${e.message}"
            listOf()
        }
    }

    fun <T : HasMetadata> List<T>.filterOutKubeSystemNss(): List<T> =
        filter { it.metadata.namespace !in listOf("kube-system", "kube-node-lease", "kube-public") }

    fun <T : HasMetadata> List<T>.filterOutDefaultNs(): List<T> =
        filter { it.metadata.namespace != "default" }

    fun <T : HasMetadata> List<T>.filterOutKubeSystemAndDefaultNss(): List<T> =
        filter { it.metadata.namespace !in listOf("kube-system", "kube-node-lease", "kube-public", "default") }

    @DslMarkerBlock
    fun <T> aggregation(
        name: String,
        collection: Iterable<T>,
        tagKey: TagMetaProviderFunction<T> = {
            if (item is HasMetadata) TagMeta(item) else throw Exception()
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