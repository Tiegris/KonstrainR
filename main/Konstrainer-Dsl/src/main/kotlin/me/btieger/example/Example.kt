package me.btieger.example

import me.btieger.dsl.*

val defaults = webhookConfigBundle {
    apiGroups(CORE)
    apiVersions(ANY)
    resources(PODS)
    namespaceSelector {
        matchLabels = mapOf(
            "managed" to "true"
        )
    }
    failurePolicy(FAIL)
    logRequest = true
    logResponse = false
}


val server = server("example-server") {
    clusterRole = ReadAny

    webhook("create-pod", defaults) {
        operations(CREATE, UPDATE)
        behavior {
            val rejectLabel = request jqx "/object/metadata/labels/reject" parseAs bool
            val podName = request jqx "/object/metadata/name" parseAs string

            allowed {
                rejectLabel != true
            }
            status {
                code = 403
                message = "You cannot do this because rejection set to $rejectLabel"
            }
            patch {
                add("/metadata/labels/app", podName ?: "null")
            }
        }
    }

    webhook("delete-pod", defaults) {
        operations(DELETE)
        behavior {
            val podName = request jqx "/oldObject/metadata/name" parseAs string
            println(podName)
            warnings {
                warning("$podName deletion was logged by ksr")
            }
        }
    }

}
