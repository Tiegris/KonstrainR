package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta

const val agentNamespace = "konstrainer-agents-ns"

val konstrainerLabels = arrayOf(
    "managedBy" to "",
    "" to ""
)

fun ObjectMeta.labels(_name: String) = apply {
    labels = mapOf(
        "app" to _name,
        *konstrainerLabels
    )
}

fun HasMetadata.metadata(_name: String) = apply {
    metadata = ObjectMeta().apply {
        name = _name
        namespace = agentNamespace
        labels(_name)
    }
}
