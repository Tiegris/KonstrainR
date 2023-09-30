package me.btieger.builtins

import me.btieger.dsl.*

const val companyPrefix = "tiegris/"
val advancedWebhookServer = server("advanced-webhook-rules") {
    simpleMonitor("Pods", {kubectl.pods().inAnyNamespace().list()}) {
        tag("Image not from company registry") {
            item.spec.containers.any { !it.image.startsWith(companyPrefix) }
        }
    }
    webhook("only-internal-registry") {
        operations(CREATE, UPDATE)
        apiGroups(APPS)
        apiVersions(ANY)
        resources(DEPLOYMENTS, STATEFULSETS, DAEMONSETS)
        namespaceSelector {
            matchLabels = mapOf(
                "managed" to "true"
            )
        }
        failurePolicy(FAIL)
        behavior {
            val podSpec = podSpec!!
            allowed {
                podSpec.containers.all { it.image.startsWith(companyPrefix) }
            }
            status {
                message = "All images must be from the company registry."
            }
        }
    }


}
