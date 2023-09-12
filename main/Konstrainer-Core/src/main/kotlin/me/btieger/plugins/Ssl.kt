package me.btieger.plugins

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.application.*
import me.btieger.Config
import me.btieger.services.helm.HelmService
import me.btieger.services.helm.create
import me.btieger.services.helm.resources.rootCaSecret
import me.btieger.services.ssl.SslService
import org.koin.ktor.ext.inject

fun Application.initSsl() {
    val helm by inject<HelmService>()
    val ssl by inject<SslService>()
    val k8s by inject<KubernetesClient>()
    val config by inject<Config>()

    k8s.secrets().inNamespace(config.namespace).withName("konstrainer-root-ca").delete()
    val rootCaSecret = helm.rootCaSecret(ssl.rootCa)
    k8s.create(rootCaSecret)
}