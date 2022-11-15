package me.btieger.persistance.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class Server(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Server>(Servers)

    var dsl by Dsl referencedOn Dsls.id
    var jar by Servers.jar
}

object Servers : IntIdTable() {
    val dsl = reference("dsl", Dsls)
    val jar = blob("jar")
}

