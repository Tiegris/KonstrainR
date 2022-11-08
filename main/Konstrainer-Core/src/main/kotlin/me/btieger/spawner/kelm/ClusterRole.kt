package me.btieger.spawner.kelm

import com.fkorotkov.openshift.newPolicyRule
import io.fabric8.openshift.api.model.ClusterRole

fun clusterRole(values: Values) =
    ClusterRole().apply {
        metadata(values)
        rules = listOf(
            newPolicyRule {
                apiGroups = listOf("admissionregistration.k8s.io")
                resources = listOf("mutatingwebhookconfigurations")
                verbs = listOf("create", "get", "delete", "list", "patch", "update")
            }
        )
    }
