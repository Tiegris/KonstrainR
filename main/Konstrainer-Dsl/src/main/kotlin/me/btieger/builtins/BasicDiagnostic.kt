package me.btieger.builtins

import me.btieger.dsl.ReadAny
import me.btieger.dsl.server

val diagnosticsServer = server("basic-diagnostics") {
    clusterRole = ReadAny

    report {
        val pods = kubelist { pods() }
        val podLabels = pods.map { it.metadata.labels }.toHashSet()

        val services = kubelist { services() }
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

        val deployments = kubelist { apps().deployments() }
        val statefulsets = kubelist { apps().statefulSets() }
        val hpas = kubelist { autoscaling().v1().horizontalPodAutoscalers() }
        val hpaRefs = hpas.map { it.spec.scaleTargetRef }
        aggregation("Deployments", deployments) {
            tag("Has HPA, but resource requests and limits are not the same") {
                (hpaRefs.any { it.name == item.metadata.name && it.apiVersion == item.apiVersion && it.kind == item.kind }) &&
                        (item.spec.template.spec.containers.any {
                            it.resources.limits["cpu"] != it.resources.requests["cpu"] ||
                                    it.resources.limits["memory"] != it.resources.requests["memory"]
                        })
            }
            tag("No resources defined") {
                item.spec.template.spec.containers.any { it.resources.limits.isNullOrEmpty() || it.resources.requests.isNullOrEmpty() }
            }
            tag("No probes") {
                item.spec.template.spec.containers.any { it.livenessProbe == null }
            }
            tag("Has long history") {
                item.spec.revisionHistoryLimit > 4
            }
            tag("No node affinity") {
                val podSpec = item.spec.template.spec
                podSpec.affinity == null && podSpec.nodeSelector.isNullOrEmpty() && podSpec.nodeName == null
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
            tag("No resources defined") {
                item.spec.template.spec.containers.any { it.resources.limits.isNullOrEmpty() || it.resources.requests.isNullOrEmpty() }
            }
            tag("No probes") {
                item.spec.template.spec.containers.any { it.livenessProbe == null }
            }
            tag("Has long history") {
                item.spec.revisionHistoryLimit > 4
            }
            tag("No node affinity") {
                val podSpec = item.spec.template.spec
                podSpec.affinity == null && podSpec.nodeSelector.isNullOrEmpty() && podSpec.nodeName == null
            }
        }

        val pvs = kubelist { persistentVolumes() }
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

        val pvcs = kubelist { persistentVolumeClaims() }
        aggregation("PVCs", pvcs) {
            tag("Unused") {
                pods.none { pod ->
                    pod.metadata.namespace == item.metadata.namespace &&
                            pod.spec.volumes.any { volume -> volume.persistentVolumeClaim?.claimName == item.metadata.name }
                }
            }
        }

        val nss = kubelist(omittedNss = none) { namespaces() }.filter { it.metadata.name !in nonUserNss }
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
            tag("Not Running") {
                item.status.containerStatuses.any { it.state.running == null }
            }
            tag("No security context") {
                item.spec.securityContext == null
            }
        }
    }


}
