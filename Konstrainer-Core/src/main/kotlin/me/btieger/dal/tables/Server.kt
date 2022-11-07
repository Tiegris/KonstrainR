package me.btieger.dal.tables

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

@Serializable
data class Server(val id: Int, val name: String, val file: String) {
    companion object {
        fun parse(row: ResultRow) = Dsl(
            id = row[Dsls.id],
            name = row[Dsls.name],
            file = row[Dsls.file],
        )
    }
}
object Servers : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("title", 128)
    val file = text("file")

    override val primaryKey = PrimaryKey(id)
}
