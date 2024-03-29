package me.btieger.webui

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.routing
import me.btieger.webui.pages.*

fun Application.configurePagesRouting() {
    configureMonitorsPageController()
    configureServersPageController()
    configureServerDetailsPageController()

    routing {
        get {
            call.respondRedirect(SERVERS_PATH,true)
        }
        get("/ui") {
            call.respondRedirect(SERVERS_PATH,true)
        }
    }
}



