package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta

const val agentNamespace = "konstrainer-agents-ns"

val konstrainerLabels = arrayOf(
    "managedBy" to "",
    "" to ""
)

fun ObjectMeta.labels(values: Values) = apply {
    labels = mapOf(
        "app" to values.name,
        *konstrainerLabels
    )
}

fun HasMetadata.metadata(values: Values) = apply {
    metadata = ObjectMeta().apply {
        name = values.name
        namespace = agentNamespace
        labels(values)
    }
}
