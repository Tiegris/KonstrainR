package me.btieger.logic.kelm.resources

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta
import me.btieger.Config

fun myLabels(_name: String) = mapOf(
    "app" to _name,
    "managedBy" to "konstrainer",
)

fun HasMetadata.metadata(_name: String, ns: String) = apply {
    metadata = ObjectMeta().apply {
        name = _name
        namespace = ns
        labels = myLabels(_name)
    }
}


fun fqdn(ns: String, svc: String): String {
    return "$svc.$ns.svc.cluster.local"
}