package me.btieger.services.helm.resources

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta

fun myLabels(_name: String, _id: Int) = mapOf(
    "app" to _name,
    "managedBy" to "konstrainer",
    "agentId" to "$_id"
)

fun HasMetadata.metadata(_name: String, ns: String, _id: Int) = apply {
    metadata = ObjectMeta().apply {
        name = _name
        namespace = ns
        labels = myLabels(_name, _id)
    }
}


fun fqdn(ns: String, svc: String): String {
    return "$svc.$ns.svc.cluster.local"
}