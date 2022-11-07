package me.btieger.persistance.tables

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


@Serializable
data class DslDto(val id: Int, val name: String)

inline val Dsl.dto
    get() = DslDto(id.value, name)

class Dsl(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Dsl>(Dsls)

    var name by Dsls.name
    var file by Dsls.file
}

object Dsls : IntIdTable() {
    val name = varchar("title", 128)
    val file = blob("file")
}
