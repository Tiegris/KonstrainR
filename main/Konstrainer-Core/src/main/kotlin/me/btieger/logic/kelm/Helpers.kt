package me.btieger.logic.kelm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient

fun KubernetesClient.create(resource: HasMetadata) {
    this.resource(resource).create()
}