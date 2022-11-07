package me.btieger.dal.tables

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

@Serializable
data class DslDto(val id: Int, val name: String)

data class Dsl(val id: Int, val name: String, val file: ByteArray) {
    companion object {
        fun parse(row: ResultRow) = Dsl(
            id = row[Dsls.id],
            name = row[Dsls.name],
            file = row[Dsls.file],
        )
        fun responseDto(row: ResultRow) = DslDto(
            id = row[Dsls.id],
            name = row[Dsls.name]
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dsl

        if (id != other.id) return false
        if (name != other.name) return false
        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + file.contentHashCode()
        return result
    }
}
object Dsls : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("title", 128)
    val file = binary("file")

    override val primaryKey = PrimaryKey(id)
}
