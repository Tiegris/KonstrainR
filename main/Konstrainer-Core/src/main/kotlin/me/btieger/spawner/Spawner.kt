package me.btieger.spawner

import io.fabric8.kubernetes.client.KubernetesClientBuilder
import me.btieger.spawner.kelm.*

val client = KubernetesClientBuilder().build()

fun spawn(values: Values) {
    val resources = listOf(
        clusterRole(values),
        clusterRoleBinding(values),
        deployment(values),
        service(values),
        serviceAccount(values),
    )
}

fun compile() {

}

fun getFile(id: Int) {

}