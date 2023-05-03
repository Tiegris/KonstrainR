package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.admissionregistration.v1.*
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration
import io.ktor.util.*
import me.btieger.Config
import me.btieger.logic.kelm.HelmService


fun HelmService.mutatingWebhookConfiguration(server: Server, cert: String, agentId: Int) =
    MutatingWebhookConfiguration().apply {
        metadata("${server.name}.btieger.me", config.namespace, agentId)
        webhooks = server.components.filterIsInstance<Webhook>().map {
            newMutatingWebhook {
                name = "${it.name}.btieger.me"
                admissionReviewVersions = listOf("v1", "v1beta1")
                sideEffects = "None"
                clientConfig {
                    caBundle = cert.encodeBase64()
                    service {
                        name = it.name
                        namespace = config.namespace
                        path = it.path
                    }
                }
                rules = listOf(
                    newRuleWithOperations {
                        operations = it.operations
                        apiGroups = it.apiGroups
                        apiVersions = it.apiVersion
                        resources = it.resources
                    }
                )
                namespaceSelector {
                    matchLabels = it.namespaceSelector
                }
                failurePolicy = it.failurePolicy
            }
        }
    }
