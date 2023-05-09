package me.btieger.webui

import io.ktor.server.html.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.routing
import me.btieger.webui.pages.*

fun Application.configurePagesRouting() {
    configureAggregationsPageController()
    configureServersPageController()
    configureServerDetailsPageController()

    routing {
        get {
            call.respondRedirect("/ui/servers",true)
        }
        get("/ui") {
            call.respondRedirect("/ui/servers",true)
        }
    }
}



