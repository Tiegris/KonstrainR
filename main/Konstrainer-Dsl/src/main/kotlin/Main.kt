import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import me.btieger.commonLibrary.EnvVarSettings

class Config : EnvVarSettings("KSR_") {
    val x by string()
}

fun main(args: Array<String>) {

    val config = Config().also {
        it.mockEnvVar("KSR_X", "asdf")
    }.apply(Config::loadAll)

    val server = me.btieger.example.server

    val k8s: KubernetesClient = KubernetesClientBuilder().build()

    val x = server.monitors.first().evaluate(k8s)

    println()

}