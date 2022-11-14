package me.btieger.spawner

import me.btieger.dsl.*
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import me.btieger.spawner.kelm.*

private val client = KubernetesClientBuilder().build()

fun spawn(server: Server) {
    client.resource(mutatingWebhookConfiguration(server)).createOrReplace()
    client.resource(service(server)).createOrReplace()
    client.resource(deployment(server)).createOrReplace()
}

fun delete(server: Server) {
    client.resource(mutatingWebhookConfiguration(server)).delete()
    client.resource(service(server)).delete()
    client.resource(deployment(server)).delete()
}
