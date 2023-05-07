package me.btieger.webui.pages

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import me.btieger.id
import me.btieger.notFound
import me.btieger.services.DslService
import me.btieger.services.ServerService
import me.btieger.webui.siteLayout
import org.koin.ktor.ext.inject

fun Application.configureServerDetailsPageController() {
    val serverService by inject<ServerService>()
    val dslService by inject<DslService>()

    routing {
        route("$SERVERS_PATH/{id}") {
            get {
                val id = call.id
                val model = dslService.getFullDetails(id)
                    ?: return@get notFound()
                call.respondHtml {
                    siteLayout {
                        serverDetails(model)
                    }
                }
            }
        }
    }
}