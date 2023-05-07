package me.btieger

import io.fabric8.kubernetes.api.model.apps.DeploymentList
import kotlinx.serialization.json.*
import me.btieger.dsl.*


val permissions = permissions {
    rule {
        apiGroups("apps")
        resources("deployments")
        verbs("get", "list")
    }
}

val server = server("example-server", permissions) {

    watch("deployments") {
        kubectl.apps().deployments().inAnyNamespace().list()
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

    aggregation("No probes") {
        forEach(watches["deployments"] as DeploymentList) {
            if (spec.template.spec.containers.any {it.livenessProbe == null}) {
                mark(YELLOW)
            }
        }
    }

}
