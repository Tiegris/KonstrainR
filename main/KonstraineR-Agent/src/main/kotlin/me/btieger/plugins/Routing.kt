package me.btieger.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.*

import me.btieger.dsl.*
import me.btieger.toBase64

fun Application.configureRouting(model: Server) {

    routing {


        get("/") {
            call.respondText("OK")
        }
        model.rules.forEach {
            val rule = it
            post(rule.path) {
                val content: JsonObject = call.receive()
                val apiVersion = content["apiVersion"]?.jsonPrimitive?.content!!
                val kind = content["kind"]?.jsonPrimitive?.content!!
                val request = content["request"]?.jsonObject!!

                val provider = rule.provider.invoke(content)

                val response = buildJsonObject {
                    this.put("apiVersion", apiVersion)
                    this.put("kind", kind)
                    val uid = request["uid"]?.jsonPrimitive?.content!!
                    this.putJsonObject("response") {
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

                call.respond(HttpStatusCode.OK, response)
            }
        }
    }



}
