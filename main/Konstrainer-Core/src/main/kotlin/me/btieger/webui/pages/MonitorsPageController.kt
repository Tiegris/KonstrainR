package me.btieger.webui.pages

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.btieger.Config
import me.btieger.services.DslService
import me.btieger.services.ServerService
import me.btieger.services.ssl.SslService
import me.btieger.webui.siteLayout
import org.koin.ktor.ext.inject

@Serializable
class MonitorsDto(val server: String, val monitors: List<MonitorDto>?, val errors: List<String>)

@Serializable
class MonitorDto(val name: String, val markedResources: List<MarkingDto>)

@Serializable
class MarkingDto(
    val fullResourceName: String,
    val name: String,
    val namespace: String?,
    val marks: List<String>,
)

const val MONITORS_PATH = "/ui/monitors"

fun Application.configureMonitorsPageController() {
    val serverService by inject<ServerService>()
    val dslService by inject<DslService>()
    val sslService by inject<SslService>()
    val config by inject<Config>()

    routing {
        authenticate {
            route(MONITORS_PATH) {
                get {
                    val aggregators = dslService.allWithAggregators()
                    val monitors = mutableListOf<MonitorsDto>()
                    HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json()
                        }
                        install(Auth) {
                            basic {
                                credentials {
                                    BasicAuthCredentials(username = config.agentUser, password = config.agentPass)
                                }
                            }
                        }
                        engine {
                            https {
                                trustManager = sslService.getTrustManager()
                            }
                        }
                    }.use { client ->
                        aggregators.forEach { item ->
                            val response = client.get("https://$item/aggregator")
                            monitors += if (response.status == HttpStatusCode.OK)
                                response.body<MonitorsDto>()
                            else
                                MonitorsDto(item, null, listOf("Error while fetching data from monitor: $item"))
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
}
