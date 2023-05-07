package me.btieger.services.ssl

import com.lordcodes.turtle.ShellScript
import com.lordcodes.turtle.shellRun
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.nio.file.Files
import java.security.KeyStore
import java.util.concurrent.ThreadLocalRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.streams.asSequence

class SslServiceOpenSslWrapperImpl : SslService {
    private val pwd = File("/app/ssl")
    private val rootCA: String
    init {
        if (pwd.exists() && !pwd.isDirectory)
            throw ExceptionInInitializerError("Ssl root dir exists, but is not a directory!")

        if (!pwd.exists()) {
            Files.createDirectories(pwd.toPath())
        }

        if (!File(pwd, "rootCA.key").exists())
            shell {
                openssl("genrsa", "-out", "rootCA.key", "4096")
            }

        if (!File(pwd, "rootCA.crt").exists())
            shell {
                openssl(
                    "req", "-x509", "-new",
                    "-nodes",
                    "-key", "rootCA.key",
                    "-sha256",
                    "-days", "365",
                    "-out", "rootCA.crt",
                    "-subj", "/C=HU/O=me.btieger/CN=konstrainer-core",
                    "-addext", "subjectAltName=DNS:konstrainer-core",
                )
            }

        if (!File(pwd, "keystore.jks").exists())
            shell {
                openssl(
                    "pkcs12", "-export",
                    "-in", "rootCA.crt",
                    "-inkey", "rootCA.key",
                    "-out", "keystore.p12",
                    "-name", "RootCA",
                    "-password", "pass:foobar",
                )
                command("keytool", listOf(
                    "-importkeystore",
                    "-srckeystore", "keystore.p12",
                    "-srcstoretype", "pkcs12",
                    "-destkeystore", "keystore.jks",
                    "-deststorepass", "foobar",
                    "-srcstorepass", "foobar",
                ))
                command("rm", listOf("keystore.p12"))
            }

        rootCA = getFile("rootCA.crt")
    }


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
        val key = getFile(keyName)
        val cert = getFile(certName)
        return SecretBundle(cert, key)
    }

    private fun getKeyStore(): KeyStore {
        val keyStoreFile = FileInputStream("${pwd.path}/keystore.jks")
        val keyStorePassword = "foobar".toCharArray()
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreFile, keyStorePassword)
        return keyStore
    }

    private fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    override fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

    private fun getFile(fileName: String) = File(pwd, fileName).readText(Charsets.US_ASCII)
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
