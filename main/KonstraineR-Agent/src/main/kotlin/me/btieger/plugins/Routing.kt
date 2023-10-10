package me.btieger.plugins

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import me.btieger.commonLibrary.toBase64
import me.btieger.commonLibrary.toJsonString
import me.btieger.dsl.Server
import me.btieger.dsl.WebhookBehaviorBuilder

fun Application.configureRouting(server: Server, k8s: KubernetesClient) {

    routing {
        get("/") {
            call.respondText("OK")
        }
        authenticate {
            if (server.hasMonitors()) {
                get("/aggregator") {
                    val monitorResponse = server.evaluateMonitors(k8s)
                    val response = buildJsonObject {
                        put("server", server.name)
                        putJsonArray("monitors") {
                            monitorResponse.results.forEach {
                                addJsonObject {
                                    put("name", it.key)
                                    put("markedResources", Json.encodeToJsonElement(it.value))
                                }
                            }
                        }
                        putJsonArray("errors") {
                            monitorResponse.errors.forEach(::add)
                        }
                    }
                    call.respond(HttpStatusCode.OK, response)
                }
            }
        }
        server.webhooks.forEach { webhook ->
            post(webhook.path) {
                val body: JsonObject = call.receive()
                if (webhook.logRequest)
                    println(body.toJsonString())
                val apiVersion = body["apiVersion"]?.jsonPrimitive?.content!!
                val kind = body["kind"]?.jsonPrimitive?.content!!
                val request = body["request"]?.jsonObject!!

                val decision = WebhookBehaviorBuilder(request, k8s).apply(webhook.provider).build()

                val response = buildJsonObject {
                    put("apiVersion", apiVersion)
                    put("kind", kind)
                    val uid = request["uid"]?.jsonPrimitive?.content!!
                    putJsonObject("response") {
                        put("uid", uid)
                        put("allowed", decision.allowed)
                        decision.warnings?.let { warnings ->
                            putJsonArray("warnings") {
                                warnings.forEach(::add)
                            }
                        }
                        if (!decision.allowed) {
                            putJsonObject("status") {
                                put("code", decision.status.code)
                                put("message", decision.status.message)
                            }
                        }
                        decision.patch?.let { patch ->
                            if (decision.allowed) {
                                put("patchType", "JSONPatch")
                                put("patch", patch.toBase64())
                            }
                        }
                    }
                }
                if (webhook.logResponse)
                    println(response.toJsonString())
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }


}
