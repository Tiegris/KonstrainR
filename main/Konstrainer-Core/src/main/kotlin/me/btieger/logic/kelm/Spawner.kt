package me.btieger.logic.kelm

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import me.btieger.dsl.Server
import me.btieger.logic.kelm.resources.deployment
import me.btieger.logic.kelm.resources.mutatingWebhookConfiguration
import me.btieger.logic.kelm.resources.service

fun KubernetesClient.create(resource: HasMetadata) {
    this.resource(resource).create()
}

//object kelm {
//
//    fun install(server: Server) {
//        client.resource(mutatingWebhookConfiguration(server)).createOrReplace()
//        client.resource(service(server)).createOrReplace()
//        client.resource(deployment(server)).createOrReplace()
//    }
//
//    fun uninstall(server: Server) {
//        client.resource(mutatingWebhookConfiguration(server)).delete()
//        client.resource(service(server)).delete()
//        client.resource(deployment(server)).delete()
//    }
//
//}
