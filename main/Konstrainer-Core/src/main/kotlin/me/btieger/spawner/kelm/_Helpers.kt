package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta

val konstrainerLabels = arrayOf(
    "managedBy" to "",
    "" to ""
)

fun HasMetadata.metadata(serviceName: String) = apply {
    metadata = ObjectMeta().apply {
        name = serviceName
        labels = mapOf (
            "app" to serviceName,
            *konstrainerLabels
        )
    }
}
