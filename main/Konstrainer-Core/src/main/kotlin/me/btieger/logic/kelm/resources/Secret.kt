package me.btieger.logic.kelm.resources

import io.fabric8.kubernetes.api.model.Secret
import io.ktor.util.*
import me.btieger.dsl.Server
import me.btieger.logic.kelm.HelmService
import me.btieger.services.ssl.SecretBundle

fun HelmService.secret(server: Server, cert: SecretBundle, agentId: Int) =
    Secret().apply {
        metadata(server.name, config.namespace, agentId)
        data = mapOf(
            "key.pem" to cert.keyPair.encodeBase64(),
            "cert.pem" to cert.certificate.encodeBase64(),
        )
    }