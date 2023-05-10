package me.btieger.webui.pages

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.btieger.services.DslService
import me.btieger.services.ServerService
import me.btieger.services.ssl.SslService
import me.btieger.webui.siteLayout
import org.koin.ktor.ext.inject

@Serializable
class MonitorsDto(val server: String, val monitors: List<MonitorDto>)

@Serializable
class MonitorDto(val name: String, val markedResources: List<MarkingDto>)

@Serializable
class MarkingDto(
    val fullResourceName: String,
    val name: String,
    val namespace: String?,
    val marks: List<String>,
)

const val MONITORS_PATH="/ui/monitors"

fun Application.configureMonitorsPageController() {
    val serverService by inject<ServerService>()
    val dslService by inject<DslService>()
    val sslService by inject<SslService>()

    routing {
        route(MONITORS_PATH) {
            get {
                val aggregators = dslService.allWithAggregators()
//                val aggregators = listOf<String>("localhost:8443")
                val monitors = mutableListOf<MonitorsDto>()
                HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json()
                    }
                    engine {
                        https {
                            trustManager = sslService.getTrustManager()
                        }
                    }
                }.use { client ->
                    aggregators.forEach { item ->
                        val response = client.get("https://$item/aggregator")
                        val x: MonitorsDto = response.body()
                        monitors += response.body<MonitorsDto>()
                    }
                }
                call.respondHtml {
                    siteLayout {
                        monitorsView(monitors)
                    }
                }
            }
        }
    }
}
