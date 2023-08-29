package me.btieger.persistance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.btieger.Config
import me.btieger.persistance.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: Config) {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:${config.home}/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Dsls)
        }
    }

    fun cleanTables() = runBlocking {
        dbQuery { Dsls.deleteAll() }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}