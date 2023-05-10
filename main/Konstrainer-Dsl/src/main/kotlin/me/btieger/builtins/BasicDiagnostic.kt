package me.btieger.builtins

import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.ServiceList
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import me.btieger.dsl.*

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

val server = server("basic-diagnostics", permissions) {

    monitor("Deployments", {kubectl.apps().deployments().inAnyNamespace().list()}) {
        mark( "Has no resources") {
            item.spec.template.spec.containers.any { it.resources.limits.isEmpty() || it.resources.requests.isEmpty() }
        }
        mark( "No node selector") {
            item.spec.template.spec.nodeSelector.isEmpty()
        }
        mark( "No probes") {
            item.spec.template.spec.containers.any {it.livenessProbe == null}
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
