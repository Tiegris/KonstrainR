package me.btieger.services.ssl

import javax.net.ssl.X509TrustManager

interface SslService {
    fun getRootCaAsPem(): String
    fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle
    fun getTrustManager(): X509TrustManager
}

class SecretBundle(val certificate: String, val keyPair: String)
