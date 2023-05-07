package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.rbac.newPolicyRule
import com.fkorotkov.kubernetes.rbac.newSubject
import com.fkorotkov.kubernetes.rbac.roleRef
import io.fabric8.kubernetes.api.model.ServiceAccount
import io.fabric8.kubernetes.api.model.rbac.ClusterRole
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding
import me.btieger.dsl.Server
import me.btieger.services.helm.HelmService

class RbacBundle(val clusterRole: ClusterRole, val clusterRoleBinding: ClusterRoleBinding, val serviceAccount: ServiceAccount)
fun HelmService.rbac(server: Server, agentId: Int) =
    server.permissions?.let {
        val cr = ClusterRole().apply {
            metadata(server.name, config.namespace, agentId)
            rules = it.map { newPolicyRule {
                apiGroups = it.apiGroups
                resources = it.resources
                verbs = it.verbs
            } }
        }
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
                name = server.name
                apiGroup = "rbac.authorization.k8s.io"
            }
        }
        val sa = ServiceAccount().apply {
            metadata(server.name, config.namespace, agentId)
        }
        RbacBundle(cr, crb, sa)
    }


