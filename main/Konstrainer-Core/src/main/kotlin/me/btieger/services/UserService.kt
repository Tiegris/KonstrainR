package me.btieger.services

import io.ktor.server.auth.*
import io.ktor.util.*
import me.btieger.persistance.DatabaseFactory.dbQuery
import me.btieger.persistance.tables.Role
import me.btieger.persistance.tables.User
import me.btieger.persistance.tables.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

data class MyPrincipal(val name: String, val role: Role) : Principal

interface UserService {
    suspend fun create(name: String, plainPass: String, role: Role): MyPrincipal
    suspend fun seedAdmin(name: String, plainPass: String)
    suspend fun authenticate(credential: UserPasswordCredential): MyPrincipal?
}

private val digestFunction = getDigestFunction("SHA-256") { "${it.length}$it${it.length}" }

class UserServiceImpl : UserService {

    override suspend fun create(name: String, plainPass: String, role: Role) = dbQuery {
        val user = User.new {
            this.name = name
            this.role = role
            password = digestFunction(plainPass)
        }
        MyPrincipal(
            user.name,
            user.role
        )
    }

    override suspend fun seedAdmin(name: String, plainPass: String) = dbQuery {
        if (Users.selectAll().count() > 0)
            return@dbQuery

        val admin = User.new {
            this.name = name
            this.role = Role.Admin
            password = digestFunction(plainPass)
        }
    }

    override suspend fun authenticate(credential: UserPasswordCredential) = dbQuery {
        User.find { (Users.name eq credential.name) and (Users.password eq digestFunction(credential.password)) }.firstOrNull()?.let {
            MyPrincipal(
                it.name,
                it.role
            )
        }
    }

}