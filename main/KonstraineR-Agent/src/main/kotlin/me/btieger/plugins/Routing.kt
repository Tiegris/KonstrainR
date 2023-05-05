package me.btieger.plugins

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.*

import me.btieger.dsl.*
import me.btieger.toBase64
import me.btieger.toJsonString

fun Application.configureRouting(server: Server, k8s: KubernetesClient) {

    routing {
        get("/") {
            call.respondText("OK")
        }
        if (server.aggregations.isNotEmpty()) {
            get("/aggregator") {
                val response = buildJsonObject {
                    put("server", server.name)
                    server.aggregations.forEach {
                        putJsonArray("aggregations") {
                            addJsonObject {
                                put("name", it.name)
                                AggregationRunner(k8s).let { runner ->
                                    it.runner.invoke(runner)
                                    putJsonArray("markings") {
                                        runner.markings.forEach { marking ->
                                            addJsonObject {
                                                put("name", marking.named)
                                                put("namespace", marking.namespace)
                                                put("status", marking.status.string)
                                                put("comment", marking.comment)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, response)
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

                val provider = webhook.provider.invoke(request)

                val response = buildJsonObject {
                    put("apiVersion", apiVersion)
                    put("kind", kind)
                    val uid = request["uid"]?.jsonPrimitive?.content!!
                    putJsonObject("response") {
                        put("uid", uid)
                        put("allowed", provider.allowed)
                        provider.warnings?.let { warnings ->
                            putJsonArray("warnings") {
                                warnings.forEach(::add)
                            }
                        }
                        if (!provider.allowed) {
                            putJsonObject("status") {
                                put("code", provider.status.code)
                                put("message", provider.status.message)
                            }
                        }
                        provider.patch?.let { patch ->
                            if (provider.allowed) {
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
