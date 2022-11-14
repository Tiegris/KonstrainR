package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.ConfigMap
import me.btieger.dsl.Server
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.util.*

private fun KeyStore.base64(): String {
    val os = ByteArrayOutputStream()
    this.store(os, "alma".toCharArray())
    return Base64.getEncoder().encodeToString(os.toByteArray())
}

fun configMap(server: Server, cert: KeyStore) =
    ConfigMap().apply {
        metadata("${server.whName}-cm")
        data = mapOf(
            "keystore.jks" to cert.base64()
        )
    }