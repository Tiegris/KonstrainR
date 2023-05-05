package me.btieger.example

import kotlinx.serialization.json.*
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

val permissions =  permissions {
    rule {
        apiGroups("")
        resources("pods")
        verbs("get")
    }
    rule {
        apiGroups()
        resources()
        verbs()
    }
}

val server = server("example-server", permissions) {

    aggregation("No resource definitions") {
        val deployments = kubectl.apps().deployments().inNamespace("demo-ns").list()
        forEach(deployments) {
            if (spec.template.spec.containers.any { it.resources?.limits == null || it.resources?.requests == null }) {
                mark(YELLOW, "No resource definition")
            }
        }
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
                add("/metadata/labels/app", podName ?: "null")
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
