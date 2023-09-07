package me.btieger.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import me.btieger.Config

fun Application.configureAuthentication(config: Config) {

    install(Authentication) {
        basic {
            validate { credentials ->
                if (credentials.name == config.authUser && credentials.password == config.authPass) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}