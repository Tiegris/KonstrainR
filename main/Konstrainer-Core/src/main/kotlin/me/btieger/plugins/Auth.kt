package me.btieger.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import me.btieger.Config
import me.btieger.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureAuthentication(config: Config) {
    val userService by inject<UserService>()

    runBlocking {
        userService.seedUsers(config.adminUser, config.adminPass)
    }

    install(Authentication) {
        basic {
            validate { credentials ->
                userService.authenticate(credentials)
            }
        }
    }
}

