package me.btieger.example

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
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

val permissions = permissions {
    rule {
        apiGroups("apps")
        resources("deployments")
        verbs("get", "list")
    }
}

val server = server("example-server", permissions) {

    monitor("All deployments", {kubectl.apps().deployments().inAnyNamespace().list()}) {
        mark( "Has no resources") {
            item.spec.template.spec.containers.any { it.resources.limits.isEmpty() || it.resources.requests.isEmpty() }
        }
    }

    webhook("create-pod", defaults) {
        path = "/create-pod"
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
        path = "/delete-pod"
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
