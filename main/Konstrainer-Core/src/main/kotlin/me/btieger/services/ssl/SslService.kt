package me.btieger.services.ssl

import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

abstract class SslService(val pwd: File) {
    protected fun getKeyStore(): KeyStore {
        val keyStoreFile = FileInputStream("${pwd.path}/keystore.jks")
        val keyStorePassword = "foobar".toCharArray()
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreFile, keyStorePassword)
        return keyStore
    }

    protected fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    abstract fun getRootCaAsPem(): String
    abstract fun deriveCert(agentServiceName: String, altnames: List<String>): SecretBundle
    abstract fun getTrustManager(): X509TrustManager
}

class SecretBundle(val certificate: String, val keyPair: String)
