package me.btieger.services.ssl

import com.lordcodes.turtle.ShellScript
import com.lordcodes.turtle.shellRun
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.security.KeyStore
import kotlin.text.toCharArray

class SslClient(val pwd: File) {

    val keyStoreFile: File
            get() = File("$pwd/keystore.jks")

    val passwdStr = "foobar"
    val passwd = passwdStr.toCharArray()

    val keyStore by lazy {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        runBlocking {
            withContext(Dispatchers.IO) {
                FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }
            }
        }
        keyStore
    }

    fun initSsl(): String {


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
                    "-password", "pass:$passwdStr",
                )
                command("keytool", listOf(
                    "-importkeystore",
                    "-srckeystore", "keystore.p12",
                    "-srcstoretype", "pkcs12",
                    "-destkeystore", "keystore.jks",
                    "-deststorepass", passwdStr,
                    "-srcstorepass", passwdStr,
                ))
                command("rm", listOf("keystore.p12"))
            }

        return getFile("rootCA.crt")
    }

    fun getFile(fileName: String) = File(pwd, fileName).readText(Charsets.US_ASCII)
    fun ShellScript.openssl(vararg args: String) = command("openssl", args.toList())
    fun shell(script: ShellScript.()->String) = shellRun(workingDirectory = pwd) {
        script()
    }
}

