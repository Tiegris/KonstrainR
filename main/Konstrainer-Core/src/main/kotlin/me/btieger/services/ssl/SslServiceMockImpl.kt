package me.btieger.services.ssl

import java.io.File
import java.security.KeyStore
import javax.net.ssl.X509TrustManager

class SslServiceMockImpl : SslService(File("build/ssl")) {

    init {
        pwd.mkdirs()
    }

    override fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle {
        return SecretBundle(getFile("agent.crt"), getFile("agent.key"))
    }

    override fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

}