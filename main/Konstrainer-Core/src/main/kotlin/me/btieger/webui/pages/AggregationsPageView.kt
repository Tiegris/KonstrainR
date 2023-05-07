package me.btieger.webui.pages

import kotlinx.html.*

fun DIV.aggregations(aggregations: MutableList<AggregationsDto>) {
    div("row") {
        aggregations.forEach { item ->
            div("col-12") {
                div("card") {
                    div("card-header") {+item.server}
                    div("card-body row") {
                        item.aggregations.forEach { a->
                            div("card") {
                                div("card-header") {+a.name}
                                div("card-body row") {
                                    a.markings.forEach { m ->
                                        div("card m-1") {
                                            div("card-header") {
                                                if (m.status == "yellow") classes += "bg-warning"
                                                if (m.status == "red") classes += "bg-error"
                                                if (m.status == "green") classes += "bg-success"
                                                +"${m.name}${m.namespace?.let{".$it"}}"
                                            }
                                            m.comment?.let {
                                                div("card-body") { +it }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}