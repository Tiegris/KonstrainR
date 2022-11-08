package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.admissionregistration.v1.newRule
import com.fkorotkov.kubernetes.newAPIGroup
import com.fkorotkov.openshift.newPolicyRule
import io.fabric8.openshift.api.model.ClusterRole

fun ClusterRole.clusterRole(serviceName: String) =
    ClusterRole().apply {
        metadata(serviceName)
        rules = listOf(
            newPolicyRule {
                apiGroups = listOf("admissionregistration.k8s.io")
                resources = listOf("mutatingwebhookconfigurations")
                verbs = listOf("create", "get", "delete", "list", "patch", "update", "watch")
            }
        )
    }
