package me.btieger

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.btieger.dsl.*

val server = server {
    whName = "example"
    serverBaseImage = ""
    whconf {
        operations(CREATE, UPDATE, DELETE, CONNECT)
        apiGroups(CORE)
        apiVersions(ANY)
        resources(PODS, DEPLOYMENTS, REPLICASETS)
        scope(ANY)
        namespaceSelector {
            matchLabels {
                "managed" eq "true"
                "istio-injection" eq "false"
            }
        }
        failurePolicy(FAIL)
    }

    rules {
        rule {
            name = "valami"
            path = "/inject"

            behavior = fun (context) = withContext {
                val kind = context["object"]?.jsonObject?.get("kind")?.jsonPrimitive?.content
                allowed {
                    kind == "Pod"
                }
                status {
                    code = 403
                    message = "You cannot do this because the type is '$kind', expected Pod."
                }
                patch {
                    add("/metadata/labels/kindInjected", kind ?: "nil")
                }
            }

        }

    }

}
