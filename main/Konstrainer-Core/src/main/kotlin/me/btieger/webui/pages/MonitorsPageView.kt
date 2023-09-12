package me.btieger.webui.pages

import kotlinx.html.DIV
import kotlinx.html.div

fun DIV.monitorsView(monitors: MutableList<MonitorsDto>) {
    div("row") {
        monitors.forEach { item ->
            div("col-12") {
                div("card") {
                    div("card-header") { +item.server }
                    if (item.monitors != null)
                        div("card-body row") {
                        item.monitors.forEach { a ->
                            div("card") {
                                div("card-header") { +a.name }
                                div("card-body row") {
                                    a.markedResources.forEach { m ->
                                        div("card m-1") {
                                            div("card-header") {
                                                +"${m.fullResourceName}/${m.name}${m.namespace?.let { ".$it" }}"
                                            }
                                            div("card-body") {
                                                +m.marks.joinToString(", ") { it }
                                            }
                                        }
                                    }
                                }


                            }

                        }
                    }
                    else
                        div("card-body") {
                            +"Error"
                        }
                }
            }
        }
    }
}