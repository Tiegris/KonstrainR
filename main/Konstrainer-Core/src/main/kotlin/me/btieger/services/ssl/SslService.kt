package me.btieger.services.ssl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

abstract class SslService(val pwd: File) {

    protected val passwdStr = "foobar"
    val passwd = passwdStr.toCharArray()

    val keyStoreFile: File
        get() = File("$pwd/keystore.jks")

    val keyStore by lazy {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        runBlocking {
            withContext(Dispatchers.IO) {
                FileInputStream(keyStoreFile).use { fis -> keyStore.load(fis, passwd) }
            }
        }
        keyStore
    }

    val rootCa by lazy {
        getFile("rootCA.crt")
    }

    protected fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        return trustManagerFactory
    }

    protected fun getFile(fileName: String) = File(pwd, fileName).readText(Charsets.US_ASCII)
    abstract fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle
    abstract fun getTrustManager(): X509TrustManager
}

class SecretBundle(val certificate: String, val keyPair: String)
