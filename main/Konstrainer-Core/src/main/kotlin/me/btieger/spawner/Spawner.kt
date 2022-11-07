package me.btieger.spawner

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.*

val client = KubernetesClientBuilder().build()

fun spawn() {
    val dep = Deployment(


    )
}

fun compile() {

}

fun getFile(id: Int) {

}