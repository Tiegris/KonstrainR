package me.btieger.persistance.services

import me.btieger.IdDto
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.DslDto
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.dto
import me.btieger.dto
import me.btieger.persistance.DatabaseFactory
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.update

interface DslService {
    suspend fun all(): List<DslDto>
    suspend fun <T> read(id: Int, transform: Dsl.() -> T): T?
    suspend fun create(name: String, content: ByteArray): IdDto?
    suspend fun update(id: Int, name: String, content: ByteArray): Boolean
    suspend fun delete(id: Int): Boolean
}

class DslServiceImpl : DslService {
    override suspend fun all() = DatabaseFactory.dbQuery {
        Dsl.all().map { it.dto }
    }

    override suspend fun <T> read(id: Int, transform: Dsl.() -> T) = DatabaseFactory.dbQuery {
        Dsl.findById(id)?.let(transform)
    }

    override suspend fun create(name: String, content: ByteArray) = DatabaseFactory.dbQuery {
        val x = Dsl.new {
            this.name = name
            this.file = ExposedBlob(content)
        }
        x.id.value.dto
    }

    override suspend fun update(id: Int, name: String, content: ByteArray) = DatabaseFactory.dbQuery {
        Dsls.update({ Dsls.id eq id }) {
            it[Dsls.name] = name
            it[file] = ExposedBlob(content)
        } > 0
    }

    override suspend fun delete(id: Int) = DatabaseFactory.dbQuery {
        Dsls.deleteWhere { Dsls.id eq id } > 0
    }

}

val dslService: DslService = DslServiceImpl()
