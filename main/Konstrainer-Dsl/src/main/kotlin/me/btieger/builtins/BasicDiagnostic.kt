package me.btieger.builtins

import me.btieger.dsl.permissions
import me.btieger.dsl.server

val permissions = permissions {
    rule {
        apiGroups("apps")
        resources("deployments")
        verbs("get", "list")
    }
    rule {
        apiGroups("")
        resources("services")
        verbs("get", "list")
    }
    rule {
        apiGroups("")
        resources("pods")
        verbs("get", "list")
    }
}

val diagnosticsServer = server("basic-diagnostics", permissions) {

    monitor("Deployments", {kubectl.apps().deployments().inAnyNamespace().list()}) {
        mark( "Has no resources") {
            item.spec.template.spec.containers.any { it.resources.limits.isNullOrEmpty() || it.resources.requests.isNullOrEmpty() }
        }
        mark( "No node selector") {
            item.spec.template.spec.nodeSelector.isEmpty()
        }
        mark( "No probes") {
            item.spec.template.spec.containers.any {it.livenessProbe == null}
        }
        mark("Has long history") {
            item.spec.revisionHistoryLimit > 4
        }
    }

    monitor("Services", {kubectl.services().inAnyNamespace().list()}) {
        mark( "Has external IP") {
            item.spec.externalIPs.isNotEmpty()
        }
    }

    monitor("Pods", {kubectl.pods().inAnyNamespace().list()}) {
        mark( "Image pull backoff") {
            item.status.containerStatuses.any { it.state.waiting?.reason == "ImagePullBackOff" }
        }
        mark( "Dangling pods") {
            item.metadata.ownerReferences.isEmpty()
        }
    }

}
