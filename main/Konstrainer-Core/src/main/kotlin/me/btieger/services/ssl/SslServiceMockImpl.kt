package me.btieger.services.ssl

import java.io.File
import javax.net.ssl.X509TrustManager

class SslServiceMockImpl : SslService(File("build/ssl")) {

    init {
        pwd.mkdirs()
    }

    override fun getRootCaAsPem(): String {
        return getFile("rootCA.crt")
    }

    override fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle {
        return SecretBundle(getFile("agent.crt"), getFile("agent.key"))
    }

    override fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

    private fun getFile(fileName: String) = File(pwd, fileName).readText(Charsets.US_ASCII)
}