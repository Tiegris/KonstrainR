package me.btieger.services.helm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient

fun KubernetesClient.create(resource: HasMetadata) {
    this.resource(resource).create()
}