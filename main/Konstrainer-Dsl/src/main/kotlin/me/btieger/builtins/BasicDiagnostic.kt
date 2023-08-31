package me.btieger.builtins

import me.btieger.dsl.server

val diagnosticsServer = server("basic-diagnostics") {

    monitor("Deployments", {kubectl.apps().deployments().inAnyNamespace().list()}) {
        tag( "Has no resources") {
            item.spec.template.spec.containers.any { it.resources.limits.isNullOrEmpty() || it.resources.requests.isNullOrEmpty() }
        }
        tag( "No node selector") {
            item.spec.template.spec.nodeSelector.isEmpty()
        }
        tag( "No probes") {
            item.spec.template.spec.containers.any {it.livenessProbe == null}
        }
        tag("Has long history") {
            item.spec.revisionHistoryLimit > 4
        }
    }

    monitor("Services", {kubectl.services().inAnyNamespace().list()}) {
        tag( "Has external IP") {
            item.spec.externalIPs.isNotEmpty()
        }
    }

    monitor("Pods", {kubectl.pods().inAnyNamespace().list()}) {
        tag( "Image pull backoff") {
            item.status.containerStatuses.any { it.state.waiting?.reason == "ImagePullBackOff" }
        }
        tag( "Dangling pods") {
            item.metadata.ownerReferences.isEmpty()
        }
    }

}
