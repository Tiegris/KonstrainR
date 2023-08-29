package me.btieger

import kotlin.test.*
import kotlinx.coroutines.runBlocking
import me.btieger.persistance.DatabaseFactory
import me.btieger.services.DslService

import org.koin.test.inject

class DslServiceTest : KonstrainerTestBase() {

    private val dslService by inject<DslService>()

    @BeforeTest
    fun beforeEach() {
        DatabaseFactory.cleanTables()
    }

    @Test
    fun `test all functions`(): Unit = runBlocking {
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