package me.btieger.persistance.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


class User(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, User>(Users)

    var name by Users.name
    var password by Users.password
    var role by Users.role
}

object Users : IntIdTable() {
    val name = varchar("name", 128)
    val password = binary("password", 32)
    val role = enumeration("role", Role::class)
}

enum class Role {
    Viewer,
    Admin,
}
