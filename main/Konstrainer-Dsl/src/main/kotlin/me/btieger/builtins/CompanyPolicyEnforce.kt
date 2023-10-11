package me.btieger.builtins

import me.btieger.dsl.*

const val companyPrefix = "tiegris/"
val companyPolicies = server("company-policies") {
    clusterRole = ReadAny
    report {
        aggregation("Pods", kubelist { pods() }) {
            tag("Image not from company registry") {
                item.spec.containers.any { !it.image.startsWith(companyPrefix) }
            }
        }

        val usersPods = kubelist(namesapce = "users") { pods() }
        val usersLogs = usersPods.associateWith {
            kubectl { pods().inNamespace(it.metadata.namespace).withName(it.metadata.name).log }
        }
        aggregation("Users App", usersLogs.entries) {
            tag("Leaks secret in logs") {
                item.value?.contains("pass", true) ?: false
            }
        }
    }
    webhook("only-internal-registry") {
        operations(CREATE, UPDATE)
        apiGroups(APPS)
        apiVersions(ANY)
        resources(DEPLOYMENTS, STATEFULSETS, DAEMONSETS)
        namespaceSelector {

        }
        failurePolicy(FAIL)
        behavior {
            allowed {
                podSpec!!.containers.all { it.image.startsWith(companyPrefix) }
            }
            status {
                message = "All images must be from the company registry."
            }
        }
    }


}
