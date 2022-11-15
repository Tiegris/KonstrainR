package me.btieger.persistance.tables

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class Dsl(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Dsl>(Dsls)

    var name by Dsls.name
    var file by Dsls.file
    var jar by Dsls.jar
}

object Dsls : IntIdTable() {
    val name = varchar("title", 128)
    val file = blob("file")
    val jar = blob("jar")
}
