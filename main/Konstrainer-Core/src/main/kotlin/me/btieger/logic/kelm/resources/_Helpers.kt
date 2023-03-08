package me.btieger.logic.kelm.resources

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta
import me.btieger.config

fun ObjectMeta.labels(_name: String) = apply {
    labels = mapOf(
        "app" to _name,
        "managedBy" to "konstrainer",
    )
}

fun HasMetadata.metadata(_name: String) = apply {
    metadata = ObjectMeta().apply {
        name = _name
        namespace = config.namespace
        labels(_name)
    }
}


fun fqdn(ns: String, svc: String): String {
    return "$svc.$ns.svc.cluster.local"
}