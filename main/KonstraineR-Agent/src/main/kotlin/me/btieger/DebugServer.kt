package me.btieger

import io.fabric8.kubernetes.api.model.LabelSelectorRequirement
import io.fabric8.kubernetes.api.model.PodSpec
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import me.btieger.dsl.*

val createUpdate_DeploymentStatefulSetDaemonSet = webhookConfigBundle {
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
}

val webhookServer = server("basic-webhook-rules") {

    webhook("hpa-resources", createUpdate_DeploymentStatefulSetDaemonSet) {
        resources(DEPLOYMENTS, STATEFULSETS)
        behavior {
            val currentObject = currentObject!!
            val hasAutoscaling = kubectl.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace().withFields(
                mapOf(
                    "spec.scaleTargetRef.apiVersion" to currentObject.apiVersion,
                    "spec.scaleTargetRef.kind" to currentObject.kind,
                    "spec.scaleTargetRef.name" to currentObject.metadata.name,
                )
            ).list().items.isNotEmpty()
            allowed {
                !hasAutoscaling || (podSpec != null && podSpec!!.containers.all {
                    it.resources?.requests != null &&
                            it.resources?.limits != null &&
                            it.resources.requests["cpu"] == it.resources.limits["cpu"]
                    it.resources.requests["memory"] == it.resources.limits["memory"]
                })
            }
            status {
                message = "${currentObject.kind} with HPA must have the same resource requests and limits!"
            }
        }
    }

    webhook("warn-no-security-context", createUpdate_DeploymentStatefulSetDaemonSet) {
        behavior {
            val securityContext = (request jqx "/object/spec/template/spec/securityContext") == buildJsonObject {  }
            warnings {
                if (podSpec?.securityContext == null) warning("No security context")
            }
        }
    }

    webhook("node-affinity", createUpdate_DeploymentStatefulSetDaemonSet) {
        behavior {
            val podSpec = (request jqx "/object/spec/template/spec")
            val affinity = podSpec jqx "affinity"
            val nodeSelector = podSpec jqx "nodeSelector"
            val nodeName = podSpec jqx "nodeName"
            allowed {
                !(affinity is JsonNull && nodeSelector is JsonNull && nodeName is JsonNull)
            }
            status {
                message = "Deployment must have some kind of node affinity! (affinity, nodeSelector, nodeName)"
            }
        }
    }
    webhook("cut-history", createUpdate_DeploymentStatefulSetDaemonSet) {
        behavior {
            val revisionHistoryLimit = (request jqx "/object/spec/revisionHistoryLimit" parseAs int) ?: 10
            warnings {
                if (revisionHistoryLimit > 4) warning("RevisionHistoryLimit was set to 4, from original: $revisionHistoryLimit")
            }
            patch {
                if (revisionHistoryLimit > 4) replace("/spec/revisionHistoryLimit", 4)
            }
        }
    }
    webhook("deny-no-resources", createUpdate_DeploymentStatefulSetDaemonSet) {
        behavior {
            val containers = podSpec?.containers ?: listOf()
            allowed {
                containers.all { it.resources?.limits?.isNotEmpty() == true && it.resources?.requests?.isNotEmpty() == true }
            }
            status {
                message = "${currentObject?.kind} must have resource definitions!"
            }
        }
    }
    webhook("warn-default-ns") {
        operations(CREATE, UPDATE, DELETE)
        apiGroups(ANY)
        apiVersions(ANY)
        resources(ANY)
        failurePolicy(IGNORE)
        logRequest = true
        namespaceSelector {
            matchExpressions = listOf(
                LabelSelectorRequirement().apply {
                    operator = "In"
                    key = "kubernetes.io/metadata.name"
                    values = listOf("default")
                }
            )
        }
        behavior {
            var ns = request jqx "/object/metadata/namespace" parseAs string
            if (ns == null) // In case of delete, the object is stored in oldObject.
                ns = request jqx "/oldObject/metadata/namespace" parseAs string
            warnings {
                warning("You are working in the namespace: $ns")
            }
        }
    }

}