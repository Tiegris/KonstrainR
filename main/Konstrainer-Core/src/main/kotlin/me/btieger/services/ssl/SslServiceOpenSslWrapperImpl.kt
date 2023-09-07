package me.btieger.services.ssl

import com.lordcodes.turtle.ShellScript
import com.lordcodes.turtle.shellRun
import me.btieger.Config
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.util.concurrent.ThreadLocalRandom
import javax.net.ssl.X509TrustManager
import kotlin.streams.asSequence

class SslServiceOpenSslWrapperImpl(config: Config) : SslService(File("${config.home}/ssl")) {

    private val client = SslClient(pwd)
    private val rootCA: String = client.initSsl()

    override fun getRootCaAsPem(): String {
        return rootCA
    }

    override fun deriveCert(agentServiceName: String, altnames: List<String>) : SecretBundle {
        var folder = randomString()
        while (File(pwd, folder).exists())
            folder = randomString()

        if (!File(pwd, folder).mkdir())
            throw Exception("Could not create folder for agent cert generation!")

        val keyName = "$folder/agent.key"
        val certName = "$folder/agent.crt"
        val csrName = "$folder/agent.csr"
        val confName = "$folder/agent.cnf"
        shell {
            openssl("genrsa", "-out", keyName, "2048")
            openssl(
                "req", "-new", "-sha256",
                "-key", keyName,
                "-subj", "/C=HU/O=me.btieger/CN=$agentServiceName",
                "-out", csrName,
            )
            BufferedWriter(FileWriter(File(pwd, confName))).use {
                it.write("subjectAltName=${altnames.joinToString(prefix = "DNS:", separator = ",DNS:")}")
            }
            openssl(
                "x509", "-req",
                "-days", "365",
                "-in", csrName,
                "-out", certName,
                "-CAkey", "rootCA.key",
                "-CA", "rootCA.crt",
                "-extfile", confName
            )
        }
        val key = client.getFile(keyName)
        val cert = client.getFile(certName)
        return SecretBundle(cert, key)
    }

    override fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

    private fun ShellScript.openssl(vararg args: String) = command("openssl", args.toList())
    private fun shell(script: ShellScript.()->String) = shellRun(workingDirectory = pwd) {
        script()
    }
}

private val charPool : List<Char> = ('a'..'z') + ('A'..'Z')

private fun randomString() = ThreadLocalRandom.current()
    .ints(32L, 0, charPool.size)
    .asSequence()
    .map(charPool::get)
    .joinToString("")
