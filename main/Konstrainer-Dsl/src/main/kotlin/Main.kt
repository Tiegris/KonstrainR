import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder

fun main(args: Array<String>) {

    val server = me.btieger.example.server

    val k8s: KubernetesClient = KubernetesClientBuilder().build()

    val x = server.monitors.first().evaluate(k8s)

    println()

}