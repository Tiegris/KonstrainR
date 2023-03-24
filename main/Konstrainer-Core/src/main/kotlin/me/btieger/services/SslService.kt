package me.btieger.services

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import sun.security.pkcs10.PKCS10
import java.io.*
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.util.*


interface  SslService {
    fun getRootCaAsPem(): String
    fun deriveCert(): KeyStore
}

class SecretBundle(val certificate: X509Certificate, val keyPair: KeyPair)


class SslServiceImpl : SslService {

    val rootCa: X509Certificate = TODO()
    val rootKey: KeyPair

    init {
        rootCa = createCa()
    }

    override fun getRootCaAsPem() =
        StringWriter().also { strWriter ->
            JcaPEMWriter(strWriter).use { pemWriter ->
                pemWriter.writeObject(rootCa)
            }
        }.toString()

    override fun deriveCert(): KeyStore {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val cert = null

        keyStore.setCertificateEntry("AgentCert", cert)
        keyStore.setKeyEntry(
            "AgentCert",
            null,
            null,
            null
        )

        return keyStore
    }
}

fun createCa(): X509Certificate {
    val keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
    val subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
    val now = Instant.now()
    val validFrom = Date.from(now)
    val validTo = Date.from(now.plusSeconds(60L * 60 * 24 * 365))

    val x500Name = X500Name("CN=My Application,O=My Organisation,L=My City,C=DE")

    val certBuilder = X509v3CertificateBuilder(
        x500Name,
        BigInteger.ONE,
        validFrom,
        validTo,
        x500Name,
        subPubKeyInfo
    )
    val signer = JcaContentSignerBuilder("SHA256WithRSA")
        .setProvider(BouncyCastleProvider())
        .build(keyPair.private)

    val x509CertificateHolder: X509CertificateHolder = certBuilder.build(signer)
    val cert: X509Certificate = CertificateFactory.getInstance("X.509")
        .generateCertificate(ByteArrayInputStream(x509CertificateHolder.encoded)) as X509Certificate

    return cert
}
