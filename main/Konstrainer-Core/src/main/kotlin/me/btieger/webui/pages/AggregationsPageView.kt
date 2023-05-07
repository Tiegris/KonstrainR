package me.btieger.webui.pages

import kotlinx.html.*

fun DIV.aggregations(aggregations: MutableList<AggregationsDto>) {
    aggregations.forEach { item ->
        div {
            p {+item.server}
            div {
                item.aggregations.forEach { a->
                    div {
                        p {+a.name}
                        div {
                            a.markings.forEach { m ->
                                div {
                                    p {+m.name}
                                    p {+m.namespace}
                                    p {+m.status}
                                    p {+m.comment}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}