package me.btieger.webui

import kotlinx.html.*
import me.btieger.webui.pages.MONITORS_PATH
import me.btieger.webui.pages.SERVERS_PATH

fun HTML.siteLayout(renderBody: DIV.()->Unit) {
    head {
        meta(charset = "utf8")
        title("KonstraineR")
        link(rel = "stylesheet", href = "/assets/bootstrap/bootstrap.css", type = "text/css")
        script { src = "/assets/jquery/jquery.min.js" }
        script { src = "/assets/bootstrap/js/bootstrap.bundle.min.js" }
        link(rel = "stylesheet", href = "/assets/styles.css", type = "text/css")
    }
    body {
        header {
            nav(classes = "navbar navbar-expand-sm navbar-toggleable-sm navbar-light bg-white border-bottom box-shadow mb-3") {
                ul(classes = "navbar-nav flex-grow-1") {
                    li(classes = "nav-item") {
                        a(classes = "nav-link text-dark", href = SERVERS_PATH) {
                            +"Servers"
                        }
                    }
                    li(classes = "nav-item") {
                        a(classes = "nav-link text-dark", href = MONITORS_PATH) {
                            +"Monitors"
                        }
                    }
                }
            }
        }
        main {
            role = "main"
            div(classes = "container") {
                renderBody()
            }
        }
        footer(classes = "footer fixed-bottom py-3") {
            div(classes = "container") {
                +"KonstraineR"
            }
        }
    }
}