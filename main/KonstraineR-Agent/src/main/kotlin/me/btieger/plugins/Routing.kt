package me.btieger.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.*

import me.btieger.dsl.*
import me.btieger.toBase64
import me.btieger.toJsonString

fun Application.configureRouting(ruleset: Server) {

    routing {
        get("/") {
            call.respondText("OK")
        }
        ruleset.rules.forEach {
            val rule = it
            post(rule.path) {
                val body: JsonObject = call.receive()
                println(body.toJsonString())
                val apiVersion = body["apiVersion"]?.jsonPrimitive?.content!!
                val kind = body["kind"]?.jsonPrimitive?.content!!
                val request = body["request"]?.jsonObject!!

                val provider = rule.provider.invoke(request)

                val response = buildJsonObject {
                    put("apiVersion", apiVersion)
                    put("kind", kind)
                    val uid = request["uid"]?.jsonPrimitive?.content!!
                    putJsonObject("response") {
                        put("uid", uid)
                        put("allowed", provider.allowed)
                        if (!provider.allowed) {
                            putJsonObject("status") {
                                put("code", provider.status.code)
                                put("message", provider.status.message)
                            }
                        }
                        if (provider.allowed) {
                            put("patchType", "JSONPatch")
                            put("patch", provider.patch.toBase64())
                        }
                    }
                }
                println(response.toJsonString())
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }



}
