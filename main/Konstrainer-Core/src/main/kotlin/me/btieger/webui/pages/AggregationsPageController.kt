package me.btieger.webui.pages

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.btieger.services.DslService
import me.btieger.services.ServerService
import me.btieger.services.ssl.SslService
import me.btieger.webui.siteLayout
import org.koin.ktor.ext.inject

@Serializable
class AggregationsDto(val server: String, val aggregations: List<AggregationDto>)
@Serializable
class AggregationDto(val name: String, val markings: List<MarkingDto>)
@Serializable
class MarkingDto(
    val name: String,
    val namespace: String?,
    val status: String,
    val comment: String?,
)

fun Application.configureAggregationsPageController() {
    val serverService by inject<ServerService>()
    val dslService by inject<DslService>()
    val sslService by inject<SslService>()

    routing {
        route("/ui/aggregations") {
            get {
                val aggregators = dslService.allWithAggregators()
                val aggregations = mutableListOf<AggregationsDto>()
                HttpClient(CIO) {
                    engine {
                        https {
                            trustManager = sslService.getTrustManager()
                        }
                    }
                }.use { client ->
                    aggregators.forEach { item ->
                        val response = client.get("https://$item/aggregator")
                        //val response = client.get("https://localhost:8443/aggregator")
                        aggregations += response.body<AggregationsDto>()
                    }
                }
                call.respondHtml {
                    siteLayout {
                        aggregations(aggregations)
                    }
                }
            }
        }
    }
}

val mockResult = mutableListOf(
    AggregationsDto(
        "s1",
        listOf(
            AggregationDto(
                "a1",
                listOf(
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                )
            ),
            AggregationDto(
                "a2",
                listOf(
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                )
            ),
        )
    ),
    AggregationsDto(
        "s2",
        listOf(
            AggregationDto(
                "a1",
                listOf(
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                )
            ),
            AggregationDto(
                "a2",
                listOf(
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                    MarkingDto("myDep", "demo-ns","yellow","alma"),
                )
            ),
        )
    ),
)