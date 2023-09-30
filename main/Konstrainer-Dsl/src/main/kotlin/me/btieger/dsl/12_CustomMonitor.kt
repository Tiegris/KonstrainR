package me.btieger.dsl

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient

typealias CustomMonitorBehaviorFunction = CustomMonitorBehaviorProvider.() -> Unit

class TagMetaProvider<T>(private val _item: T) {
    @DslMarkerConstant
    val item = _item
}
typealias TagMetaProviderFunction<T> = TagMetaProvider<T>.() -> TagMeta

class CustomMonitorBehaviorProvider(private val _kubectl: KubernetesClient) {

    @DslMarkerConstant
    val kubectl = _kubectl

    private val _aggregations = mutableListOf<Aggregation>()
    fun getAggregations() = _aggregations

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
        for (item in collection) {
            val key = TagMetaProvider(item).run(tagKey)
            val marks = AggregationBuilder(item).apply(setup).markers.filter { it.condition() }.map { it.status }
            if (marks.isNotEmpty())
                tags += Tag(key, marks)
        }

        _aggregations += Aggregation(name, tags)
    }

}

class AggregationBuilder<T>(private val _item: T) {
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
    val collection: List<Tag>
)