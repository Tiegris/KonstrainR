package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.rbac.newSubject
import com.fkorotkov.kubernetes.rbac.roleRef

import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding


fun clusterRoleBinding(values: Values) =
    ClusterRoleBinding().apply {
        metadata(values)
        roleRef {
            kind = "ClusterRole"
            name = values.name
            apiGroup = "rbac.authorization.k8s.io"
        }
        subjects = listOf(
            newSubject {
                kind = "ServiceAccount"
                name = values.name
                namespace = agentNamespace
            }
        )
    }
