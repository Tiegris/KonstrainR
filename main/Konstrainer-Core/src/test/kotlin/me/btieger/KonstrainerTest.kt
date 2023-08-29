package me.btieger

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.just
import io.mockk.mockkClass
import io.mockk.runs
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.controllers.serverController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.configureSerialization
import me.btieger.services.DslService
import me.btieger.services.DslServiceImpl
import me.btieger.services.ServerService
import me.btieger.services.ServerServiceImpl
import me.btieger.services.helm.HelmService
import me.btieger.services.helm.create
import me.btieger.services.ssl.SslService
import me.btieger.services.ssl.SslServiceMockImpl
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock

open class KonstrainerTest : KoinTest {

    @KtorDsl
    fun konstrainerTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            configureSerialization()
            echoController()
            dslController()
            serverController()
        }
        block()
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        val config = Config().apply {
            mockEnvVar("KSR_SERVICE_NAME", "localhost:8080")
        }.apply(Config::loadAll)
        val k8sMock = declareMock<KubernetesClient>()
        every { k8sMock.create(any()) } just runs
        modules(module {
            single(createdAtStart = true) { config }
            single<KubernetesClient>(createdAtStart = true) { k8sMock }
            single<ServerService>(createdAtStart = true) { ServerServiceImpl(get(), get(), get(), get()) }
            single<DslService>(createdAtStart = true) { DslServiceImpl(get(), get(), get(), get()) }
            single(createdAtStart = true) { HelmService(get()) }
            single<SslService>(createdAtStart = true) { SslServiceMockImpl() }
        })
        DatabaseFactory.init(config)
    }

}