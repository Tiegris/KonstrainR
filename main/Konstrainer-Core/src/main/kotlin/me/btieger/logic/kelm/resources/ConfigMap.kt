package me.btieger.logic.kelm.resources

import io.fabric8.kubernetes.api.model.Secret
import me.btieger.dsl.Server
import me.btieger.logic.kelm.HelmService
import me.btieger.services.ssl.SecretBundle

fun HelmService.secret(server: Server, cert: SecretBundle) =
    Secret().apply {
        metadata(server.whName, config.namespace)
        data = mapOf(
            "key.pem" to cert.keyPair,
            "cert.pem" to cert.certificate,
        )
    }