package me.btieger.persistance.services

import me.btieger.domain.CreateServerDto
import me.btieger.domain.ServerDto
import me.btieger.domain.dto
import me.btieger.dto
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.Server
import me.btieger.logic.spawner.Kelm
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface ServerService {
    suspend fun all(): List<ServerDto>
    suspend fun <T> read(id: Int, transform: Server.() -> T): T?
    suspend fun create(req: CreateServerDto): ServerDto?
    suspend fun delete(id: Int): Boolean
}

class ServerServiceImpl : ServerService {
    override suspend fun all() = DatabaseFactory.dbQuery {
        Server.all().map { it.dto }
    }

    override suspend fun <T> read(id: Int, transform: Server.() -> T) = DatabaseFactory.dbQuery {
        Server.findById(id)?.let(transform)
    }

    override suspend fun create(req: CreateServerDto) = DatabaseFactory.dbQuery {
        val dsl = Dsl.findById(req.dslId) ?: return@dbQuery null

        val x = Server.new {
            this.dsl = dsl
        }
        x.dto
    }

    override suspend fun delete(id: Int) = DatabaseFactory.dbQuery {
        val server = Server.findById(id) ?: return@dbQuery false
        TODO()
    }

}

val serverService = ServerServiceImpl()