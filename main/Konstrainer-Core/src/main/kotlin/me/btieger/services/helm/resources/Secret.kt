package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.metadata
import io.fabric8.kubernetes.api.model.Secret
import io.ktor.util.*
import me.btieger.dsl.Server
import me.btieger.services.helm.HelmService
import me.btieger.services.ssl.SecretBundle

fun HelmService.secret(server: Server, cert: SecretBundle, rootCa: String, agentId: Int) =
    Secret().apply {
        metadata(server.name, config.namespace, agentId)
        data = mapOf(
            "key.pem" to cert.keyPair.encodeBase64(),
            "cert.pem" to cert.certificate.encodeBase64(),
            "rootCa.pem" to rootCa.encodeBase64()
        )
    }

fun HelmService.rootCaSecret(rootCa: String) =
    Secret().apply {
        metadata {
            name = "konstrainer-root-ca"
            namespace =  config.namespace
            labels = mapOf(
                "managedBy" to "konstrainer"
            )
        }
        data = mapOf(
            "rootCa.pem" to rootCa.encodeBase64()
        )
    }