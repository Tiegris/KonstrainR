package me.btieger.builtins

import me.btieger.dsl.ReadAny
import me.btieger.dsl.TagMeta
import me.btieger.dsl.server

val diagnosticsServer = server("basic-diagnostics") {
    clusterRole = ReadAny

    complexMonitoring {
        val pods = kubectl.pods().inAnyNamespace().list().items
        val podLogs = pods.associate { pod ->
            TagMeta(pod) to kubectl.pods().inNamespace(pod.metadata.namespace)
                .withName(pod.metadata.name).log
        }
        aggregation("Apps", podLogs.entries, keyName = { item.key }) {
            tag("Leaking potential secrets") {
                val log = item.value.lowercase()
                log.contains("pass") || log.contains("secret") || log.contains("token")
            }
        }

        val podLabels = pods.map { it.metadata.labels }.toHashSet()
        val services = kubectl.services().inAnyNamespace().list().items
        aggregation("Services", services) {
            tag("No backend") {
                podLabels.none { podLabel ->
                    item.spec.selector.all { podLabel.entries.contains(it) }
                }
            }
        }

    }

    monitor("Deployments", { kubectl.apps().deployments().inAnyNamespace().list() }) {
        tag("Has no resources") {
            item.spec.template.spec.containers.any { it.resources.limits.isNullOrEmpty() || it.resources.requests.isNullOrEmpty() }
        }
        tag("No node selector") {
            item.spec.template.spec.nodeSelector.isEmpty()
        }
        tag("No probes") {
            item.spec.template.spec.containers.any { it.livenessProbe == null }
        }
        tag("Has long history") {
            item.spec.revisionHistoryLimit > 4
        }
    }

    monitor("Services", { kubectl.services().inAnyNamespace().list() }) {
        tag("Has external IP") {
            item.spec.externalIPs.isNotEmpty()
        }
    }

    monitor("Pods", { kubectl.pods().inAnyNamespace().list() }) {
        tag("Image pull backoff") {
            item.status.containerStatuses.any { it.state.waiting?.reason == "ImagePullBackOff" }
        }
        tag("Dangling pods") {
            item.metadata.ownerReferences.isEmpty()
        }
    }

}
