package me.btieger.services.ssl

import java.io.File

class SslServiceMockImpl : SslService {

    private val pwd = File("build/ssl")
    init {
        pwd.mkdirs()
    }

    override fun getRootCaAsPem(): String {
        return getFile("rootCA.crt")
    }

    override fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle {
        return SecretBundle(getFile("agent.crt"), getFile("agent.key"))
    }

    private fun getFile(fileName: String) = File(pwd, fileName).readText(Charsets.US_ASCII)
}