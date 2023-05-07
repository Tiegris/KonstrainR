import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import kotlinx.serialization.json.*
import me.btieger.dsl.*

fun main(args: Array<String>) {

    val k = me.btieger.example.server

    val k8s: KubernetesClient = KubernetesClientBuilder().build()

    val map = mutableMapOf<String, KubernetesResourceList<out HasMetadata>>()
    k.watches.forEach {
        map[it.key] = WatchRunner(k8s).run(it.value)
    }

    val x =
    k.watches.mapValues {
        WatchRunner(k8s).run(it.value)
    }


    println()

}