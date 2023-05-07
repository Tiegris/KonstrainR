package me.btieger.webui.pages

import kotlinx.html.*

import me.btieger.domain.DslFullDetailedDto

fun DIV.serverDetails(model: DslFullDetailedDto) {
    div("card") {
        div("card-header") {
            +"${model.name}, ID: ${model.id.toString()}"
        }

        ul("list-group list-group-flush") {
            li("list-group-item") {
                p { +"Build status ${model.buildStatus}" }
                model.buildStartTimestamp?.let {
                    p { +"Build start timestamp $it" }
                }
                model.buildError?.let {
                    p { +"Build error $it" }
                }
            }
            model.serverStatus?.let {
                li("list-group-item") {
                    p { +"Server status $it" }
                    model.hasWebhooks?.let {
                        p { +"Has Webhooks $it" }
                    }
                    model.hasAggregators?.let {
                        p { +"Has Aggregators $it" }
                    }
                }
            }
        }
        div("card-body multiline") {
            +model.file
        }
    }
}