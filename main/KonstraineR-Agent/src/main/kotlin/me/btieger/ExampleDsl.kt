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
                "istio-injectio" eq "false"
            }
        }
        failurePolicy(FAIL)
    }

    rules {
        rule {
            name = "valami"
            path = "/mutate"

            behavior = fun (context) = withContext {
                allowed {
                    context["object"]?.jsonObject?.get("kind")?.jsonPrimitive?.content == "Pod"
                }
                status {
                    code = 403
                    message = "You cannot do this because it is Tuesday and your name starts with A"
                }
                patch {
                    add("/metadata/labels/alma", context["object"]?.jsonObject?.get("kind")?.jsonPrimitive?.content ?: "nil")
                }
            }


        }

    }

}
