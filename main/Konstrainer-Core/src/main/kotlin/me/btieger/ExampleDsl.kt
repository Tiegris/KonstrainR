package me.btieger

import kotlinx.serialization.json.*
import me.btieger.dsl.*

val server = server("example-server") {
    webhook("create-pod") {
        path = "/create-pod"
        operations(CREATE, UPDATE)
        apiGroups(CORE)
        apiVersions(ANY)
        resources(PODS)
        scope(ANY)
        namespaceSelector {
            matchLabels {
                "managed" eq "true"
            }
        }
        failurePolicy(FAIL)
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

    webhook("delete-pod") {
        path = "/create-pod"
        operations(DELETE)
        apiGroups(CORE)
        apiVersions(ANY)
        resources(PODS)
        scope(ANY)
        namespaceSelector {
            matchLabels {
                "managed" eq "true"
            }
        }
        failurePolicy(FAIL)
        behavior = fun (context) = withContext {
            val podName = context jqx "/oldObject/metadata/name" parseAs string
            println(podName)
        }

    }

}
