import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import me.btieger.builtins.diagnosticsServer
import me.btieger.commonLibrary.EnvVarSettings

class Config : EnvVarSettings("KSR_") {
    val x by string()
}

fun main(args: Array<String>) {

    val kubectl: KubernetesClient = KubernetesClientBuilder().build()

    val x = diagnosticsServer.evaluateMonitors(kubectl)

    println(x)
}