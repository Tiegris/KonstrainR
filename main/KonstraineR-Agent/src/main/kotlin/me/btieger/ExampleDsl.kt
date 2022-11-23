package me.btieger

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
                val kind = context jqx "/object/kind" parseAs string
                val rejectLabel = context jqx "/object/metadata/labels/reject" parseAs bool
                println(rejectLabel)
                allowed {
                    rejectLabel != true
                }
                status {
                    code = 403
                    message = "You cannot do this because rejection set to $rejectLabel"
                }
                patch {
                    add("/metadata/labels/kindInjected", kind ?: "nil")
                }
            }

        }

    }

}
