package me.btieger

import io.fabric8.kubernetes.client.KubernetesClient
import io.mockk.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import me.btieger.persistance.DatabaseFactory
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
import org.koin.test.inject
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock

class DslServiceTest : KoinTest {

    val dslService by inject<DslService>()

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

    @BeforeTest
    fun beforeEach() {
        DatabaseFactory.cleanTables()
    }

    @Test
    fun `should run without error`(): Unit = runBlocking {
        val dslFileAsString = "lorem ipsum"
        val dslFileAsByteArray = dslFileAsString.toByteArray()

        val createResult = dslService.createDsl("demo", dslFileAsByteArray)
        assertNotNull(createResult)

        val id = createResult.id
        val getAllResult = dslService.all()
        assertNotNull(getAllResult)
        assertEquals(1, getAllResult.size)

        val getDetailsResult = dslService.getDetails(id)
        assertNotNull(getDetailsResult)

        val getFullDetailsResult = dslService.getFullDetails(id)
        assertNotNull(getFullDetailsResult)
        assertEquals(getFullDetailsResult.file, dslFileAsString)

        val getFileResult = dslService.getFile(id)
        assertNotNull(getFileResult)
        assertContentEquals(dslFileAsByteArray, getFileResult)

        val getJarResult = dslService.getJar(id)
        assertNull(getJarResult)

        val allWithAggregatorsResult = dslService.allWithAggregators()
        assertNotNull(allWithAggregatorsResult)

        val deleteResult = dslService.deleteDsl(id)
        assertTrue(deleteResult)

        val getAllAfterDeleteResult = dslService.all()
        assertEquals(0, getAllAfterDeleteResult.size)
    }

}