package me.btieger.webui

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.css.*

fun Application.configureCSS() {

    routing {
        get("/assets/styles.css") {
            call.respondCss {
                rule(".multiline") {
                    whiteSpace = WhiteSpace.preWrap
                }
                rule(".footer") {
                    position = Position.absolute
                    bottom = LinearDimension("0")
                    width = LinearDimension("100%")
                    height = LinearDimension("60px")
                    backgroundColor = Color("#f5f5f5")
                }
                body {
                    marginBottom = LinearDimension("70px")
                }
                html {
                    position = Position.relative
                    minHeight = LinearDimension("100%")
                }
                p {
                    marginBottom = LinearDimension("0")
                }
            }
        }

    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
