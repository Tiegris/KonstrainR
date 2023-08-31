package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.rbac.newPolicyRule
import com.fkorotkov.kubernetes.rbac.newSubject
import com.fkorotkov.kubernetes.rbac.roleRef
import io.fabric8.kubernetes.api.model.ServiceAccount
import io.fabric8.kubernetes.api.model.rbac.ClusterRole
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding
import me.btieger.dsl.Server
import me.btieger.services.helm.HelmService

class RbacBundle(val clusterRoleBinding: ClusterRoleBinding, val serviceAccount: ServiceAccount)
fun HelmService.rbac(server: Server, agentId: Int) =
    server.clusterRole?.let { clusterRoleName ->
        val crb = ClusterRoleBinding().apply {
            metadata(server.name, config.namespace, agentId)
            subjects = listOf(
                newSubject {
                    kind = "ServiceAccount"
                    name = server.name
                    namespace = config.namespace
                }
            )
            roleRef {
                kind = "ClusterRole"
                name = clusterRoleName
                apiGroup = "rbac.authorization.k8s.io"
            }
        }
        val sa = ServiceAccount().apply {
            metadata(server.name, config.namespace, agentId)
        }
        RbacBundle(crb, sa)
    }


