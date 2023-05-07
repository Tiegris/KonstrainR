package btieger.me.plugins.web

import btieger.me.plugins.web.pages.aggregations
import btieger.me.plugins.web.pages.servers
import io.ktor.server.html.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        get {
            call.respondRedirect("/servers",true)
        }
        get("/servers") {
            call.respondHtml {
                siteLayout {
                    servers()
                }
            }
        }
        get("/aggregations") {
            call.respondHtml {
                siteLayout {
                    aggregations()
                }
            }
        }
    }
}



