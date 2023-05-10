package me.btieger.webui.pages

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.btieger.*
import me.btieger.domain.DslConciseDto
import me.btieger.services.DslService
import me.btieger.webui.siteLayout
import org.koin.ktor.ext.inject

const val SERVERS_PATH = "/ui/servers"
class ServersPageModel(val dsls: List<DslConciseDto>)

fun Application.configureServersPageController() {
    val dslService by inject<DslService>()

    routing {
        route(SERVERS_PATH) {
            get {
                val model = ServersPageModel(dslService.all())
                call.respondHtml {
                    siteLayout {
                        serversView(model)
                    }
                }
            }
            post {
                exceptionGuard {
                    val multipartData = call.receiveMultipart()
                    multipartData.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val filename = part.originalFileName
                                ?: return@forEachPart badRequest("No file name")
                            val fileBytes = part.streamProvider().readBytes()
                            val result = dslService.createDsl(filename, fileBytes)
                                ?: return@forEachPart internalServerError("Could not upload file")
                            call.respondRedirect(SERVERS_PATH)
                        }
                    }
                }
            }
        }
    }
}