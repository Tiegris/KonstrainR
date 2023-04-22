package me.btieger.services.ssl

interface SslService {
    fun getRootCaAsPem(): String
    fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle
}

class SecretBundle(val certificate: String, val keyPair: String)
