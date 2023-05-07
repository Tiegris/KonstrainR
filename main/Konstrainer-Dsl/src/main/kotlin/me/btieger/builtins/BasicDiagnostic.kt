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

    watch("deployments") {
        kubectl.apps().deployments().list()
    }

    watch("services") {
        kubectl.services().list()
    }

    watch("pods") {
        kubectl.pods().list()
    }

    aggregation("No resource definitions") {
        forEach(watches["deployments"] as DeploymentList) {
            if (spec.template.spec.containers.any { it.resources.limits.isEmpty() || it.resources.requests.isEmpty() }) {
                mark(YELLOW)
            }
        }
    }

    aggregation("No Node Selectors") {
        forEach(watches["deployments"] as DeploymentList) {
            if (spec.template.spec.nodeSelector.isEmpty()) {
                mark(YELLOW)
            }
        }
    }

    aggregation("No livenessProbe") {
        forEach(watches["deployments"] as DeploymentList) {
            if (spec.template.spec.containers.any {it.livenessProbe == null}) {
                mark(YELLOW)
            }
        }
    }

    aggregation("Service has external IP") {
        forEach(watches["services"] as ServiceList) {
            if (spec.externalIPs.isNotEmpty()) {
                mark(YELLOW)
            }
        }
    }

    aggregation("Dangling Pods") {
        forEach(watches["pods"] as PodList) {
            if (metadata.ownerReferences.isEmpty()) {
                mark(RED)
            }
        }
    }

}
