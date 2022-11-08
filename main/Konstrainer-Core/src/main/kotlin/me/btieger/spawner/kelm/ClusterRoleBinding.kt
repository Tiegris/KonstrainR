package me.btieger.spawner.kelm

import io.fabric8.openshift.api.model.ClusterRoleBinding

fun ClusterRoleBinding.clusterRoleBinding(serviceName: String) =
    ClusterRoleBinding().apply {
        metadata(serviceName)
    }
