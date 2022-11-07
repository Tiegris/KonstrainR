package me.btieger.dal

import kotlinx.coroutines.runBlocking
import me.btieger.dal.tables.Dsl
import me.btieger.dal.tables.DslDto
import me.btieger.dal.tables.Dsls
import org.jetbrains.exposed.sql.*

interface DslDao {
    suspend fun all(): List<DslDto>
    suspend fun <T> read(id: Int, transform: (ResultRow)->T): T?
    suspend fun create(name: String, content: ByteArray): Int?
    suspend fun update(id: Int, name: String, content: ByteArray): Boolean
    suspend fun delete(id: Int): Boolean
}

class DslDaoImpl : DslDao {
    override suspend fun all() = DatabaseFactory.dbQuery {
        Dsls.selectAll().map(Dsl::responseDto)
    }

    override suspend fun <T> read(id: Int, transform: (ResultRow)->T) = DatabaseFactory.dbQuery {
        Dsls
            .select { Dsls.id eq id }
            .map(transform)
            .singleOrNull()
    }

    override suspend fun create(name: String, content: ByteArray) = DatabaseFactory.dbQuery {
        val insertStatement = Dsls.insert {
            it[Dsls.name] = name
            it[Dsls.file] = content
        }
        insertStatement.resultedValues?.singleOrNull()?.let {
            it[Dsls.id]
        }
    }

    override suspend fun update(id: Int, name: String, content: ByteArray) = DatabaseFactory.dbQuery {
        Dsls.update({ Dsls.id eq id }) {
            it[Dsls.name] = name
            it[Dsls.file] = content
        } > 0
    }

    override suspend fun delete(id: Int) = DatabaseFactory.dbQuery {
        Dsls.deleteWhere { Dsls.id eq id } > 0
    }

}

val dslDao: DslDao = DslDaoImpl().apply {
    runBlocking {
        if(all().isEmpty()) {
            create("proba.kts", byteArrayOf("""
                val x = server {
                }
            """.trimIndent().toByte()))
        }
    }
}