package me.btieger.controllers

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.btieger.services.DslService
import me.btieger.services.ssl.SslServiceMockImpl
import me.btieger.services.ssl.SslServiceOpenSslWrapperImpl
import org.koin.ktor.ext.inject

fun Application.echoController() {
    routing {
        get("/") {
            call.respond("got it")
        }
        get("/debug") {
            call.respond("ok")
        }
    }
}