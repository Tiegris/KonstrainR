package me.btieger

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import me.btieger.dsl.*

val defaults = webhookConfigBundle {
    apiGroups(CORE)
    apiVersions(ANY)
    resources(PODS)
    namespaceSelector {
        matchLabels {
            "managed" eq "true"
        }
    }
    failurePolicy(FAIL)
    logRequest = true
    logResponse = true
}

val server = server("example-server") {

    warningAggregator {

    }

    webhook("create-pod", defaults) {
        path = "/create-pod"
        operations(CREATE, UPDATE)
        behavior = fun (context) = withContext {
            val rejectLabel = context jqx "/object/metadata/labels/reject" parseAs bool
            val podName = context jqx "/object/metadata/name" parseAs string

            allowed {
                rejectLabel != true
            }
            status {
                code = 403
                message = "You cannot do this because rejection set to $rejectLabel"
            }
            patch {
                add("/metadata/labels/app", podName)
                add("/spec/containers/-",
                    buildJsonObject {
                        put("name", "$podName-sidecar")
                        put("image", "debian:11")
                        putJsonArray("command") {
                            add("sleep")
                            add("infinity")
                        }
                    }
                )
            }
        }

    }

    webhook("delete-pod", defaults) {
        path = "/delete-pod"
        operations(DELETE)
        behavior = fun (context) = withContext {
            val podName = context jqx "/oldObject/metadata/name" parseAs string
            println(podName)
            warnings {
                warning("$podName deletion was logged by ksr")
            }
        }

    }

}