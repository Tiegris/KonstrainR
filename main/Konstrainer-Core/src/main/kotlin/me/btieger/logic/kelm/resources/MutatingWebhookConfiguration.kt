package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.admissionregistration.v1.*
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration


fun mutatingWebhookConfiguration(server: Server) =
    MutatingWebhookConfiguration().apply {
        metadata(server.whName)
        webhooks = server.rules.map {
            newMutatingWebhook {
                name = server.whName
                admissionReviewVersions = listOf("v1", "v1beta1")
                sideEffects = "None"
                clientConfig {
                    caBundle = "TODO caAsString"
                    service {
                        name = server.whName
                        namespace = agentNamespace
                        path = "TODO"
                    }
                }
                rules = listOf(
                    newRuleWithOperations {
                        operations = server.whConf.operations
                        apiGroups = server.whConf.apiGroups
                        apiVersions = server.whConf.apiVersion
                        resources = server.whConf.resources
                    }
                )
                namespaceSelector {
                    matchLabels = server.whConf.namespaceSelector
                }
                failurePolicy = server.whConf.failurePolicy
            }
        }
    }
