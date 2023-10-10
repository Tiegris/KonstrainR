package me.btieger.builtins

import me.btieger.dsl.ReadAny
import me.btieger.dsl.server

val diagnosticsServer = server("basic-diagnostics") {
    clusterRole = ReadAny

    report {
        val pods = kubectl.pods().inAnyNamespace().list().items
        val podLabels = pods.map { it.metadata.labels }.toHashSet()
        val services = kubectl.services().inAnyNamespace().list().items
        aggregation("Services", services) {
            tag("Has external IP") {
                item.spec.externalIPs.isNotEmpty()
            }
            tag("No backend") {
                podLabels.none { podLabel ->
                    item.spec.selector.all { podLabel.entries.contains(it) }
                }
            }
        }

        val deployments = kubectl.apps().deployments().inAnyNamespace().list().items
        val statefulsets = kubectl.apps().statefulSets().inAnyNamespace().list().items
        val hpas = kubectl.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace().list().items
        val hpaRefs = hpas.map { it.spec.scaleTargetRef }
        aggregation("Deployments", deployments) {
            tag("Has HPA, but resource requests and limits are not the same") {
                (hpaRefs.any { it.name == item.metadata.name && it.apiVersion == item.apiVersion && it.kind == item.kind }) &&
                        (item.spec.template.spec.containers.any {
                            it.resources.limits["cpu"] != it.resources.requests["cpu"] ||
                                    it.resources.limits["memory"] != it.resources.requests["memory"]
                        })
            }
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
            tag("No node affinity") {
                val podSpec = item.spec.template.spec
                podSpec.affinity == null && podSpec.nodeSelector == null && podSpec.nodeName == null
            }
        }
        aggregation("StatefulSets", statefulsets) {
            tag("Has HPA, but resource requests and limits are not the same") {
                (hpaRefs.any { it.name == item.metadata.name && it.apiVersion == item.apiVersion && it.kind == item.kind }) &&
                        (item.spec.template.spec.containers.any {
                            it.resources.limits["cpu"] != it.resources.requests["cpu"] ||
                                    it.resources.limits["memory"] != it.resources.requests["memory"]
                        })
            }
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
            tag("No node affinity") {
                val podSpec = item.spec.template.spec
                podSpec.affinity == null && podSpec.nodeSelector == null && podSpec.nodeName == null
            }
        }

        val pvs = kubectl.persistentVolumes().list().items
        aggregation("Volumes", pvs) {
            tag("Released state") {
                item.status.phase == "Released"
            }
            tag("Available state") {
                item.status.phase == "Available"
            }
            tag("Failed state") {
                item.status.phase == "Failed"
            }
        }

        val pvcs = kubectl.persistentVolumeClaims().inAnyNamespace().list().items
        aggregation("PVCs", pvcs) {
            tag("Unused") {
                pods.none { pod ->
                    pod.metadata.namespace == item.metadata.namespace &&
                            pod.spec.volumes.any { volume -> volume.persistentVolumeClaim?.claimName == item.metadata.name }
                }
            }
        }

        val nss = kubectl.namespaces().list().items
        aggregation("Namespaces", nss) {
            tag("Has no pods") {
                pods.none { pod -> pod.metadata.namespace == item.metadata.name }
            }
        }

        aggregation("Pods", pods) {
            tag("Not running or not succeeded") {
                item.status.phase != "Running" && item.status.phase != "Succeeded"
            }
            tag("Image pull backoff") {
                item.status.containerStatuses.any { it.state.waiting?.reason == "ImagePullBackOff" } ||
                        item.status.containerStatuses.any { it.state.waiting?.reason == "ErrImagePull" }
            }
            tag("Dangling pod") {
                item.metadata.ownerReferences.isEmpty()
            }
            tag("NotRunning") {
                item.status.containerStatuses.any { it.state.running == null }
            }
            tag("No security context") {
                item.spec.securityContext == null
            }
        }
    }


}
