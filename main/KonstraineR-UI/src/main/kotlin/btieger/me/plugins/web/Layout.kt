package btieger.me.plugins.web

import kotlinx.html.*

fun HTML.siteLayout(renderBody: MAIN.()->Unit) {
    head {
        meta(charset = "utf8")
        title("KonstraineR")
        link(rel = "stylesheet", href = "/assets/bootstrap/bootstrap.css", type = "text/css")
        script {
            src = "/assets/bootstrap/js/bootstrap.bundle.min.js"
        }
        link(rel = "stylesheet", href = "/assets/styles.css", type = "text/css")
    }
    body {
        header {
            nav(classes = "navbar navbar-expand-sm navbar-toggleable-sm navbar-light bg-white border-bottom box-shadow mb-3") {
                ul(classes = "navbar-nav flex-grow-1") {
                    li(classes = "nav-item") {
                        a(classes = "nav-link text-dark", href = "/servers") {
                            +"Servers"
                        }
                    }
                    li(classes = "nav-item") {
                        a(classes = "nav-link text-dark", href = "/aggregations") {
                            +"Aggregations"
                        }
                    }
                }
            }
        }
        div(classes = "container") {
            main(classes="pb-3") {
                renderBody()
            }
        }
        footer(classes = "border-top footer fixed-bottom text-muted") {
            div(classes = "container") {
                +"KonstraineR"
            }
        }
    }
}