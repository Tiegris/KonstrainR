package btieger.me.plugins.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.css.*

fun Application.configureCSS() {

    routing {
        get("/assets/styles.css") {
            call.respondCss {
//                body {
//                    backgroundColor = Color.darkBlue
//                    margin(0.px)
//                }
//                rule("h1.page-title") {
//                    color = Color.white
//                }
            }
        }

    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
