package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.admissionregistration.v1.*
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration
import me.btieger.Config
import me.btieger.logic.kelm.HelmService


fun HelmService.mutatingWebhookConfiguration(server: Server, cert: String) =
    MutatingWebhookConfiguration().apply {
        metadata(server.whName, config.namespace)
        webhooks = server.rules.map {
            newMutatingWebhook {
                name = server.whName
                admissionReviewVersions = listOf("v1", "v1beta1")
                sideEffects = "None"
                clientConfig {
                    caBundle = cert
                    service {
                        name = server.whName
                        namespace = config.namespace
                        path = server.rules[0].path // TODO
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
