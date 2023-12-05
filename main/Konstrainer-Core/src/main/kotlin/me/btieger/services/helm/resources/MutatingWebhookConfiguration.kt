package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.admissionregistration.v1.*
import com.fkorotkov.kubernetes.newLabelSelector
import io.fabric8.kubernetes.api.model.LabelSelector
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration
import io.ktor.util.*
import me.btieger.services.helm.HelmService


fun HelmService.mutatingWebhookConfiguration(server: Server, cert: String, agentId: Int) =
    MutatingWebhookConfiguration().apply {
        metadata("${server.name}.btieger.me", config.namespace, agentId)
        webhooks = server.webhooks.map {
            newMutatingWebhook {
                name = "${it.name}.btieger.me"
                admissionReviewVersions = listOf("v1", "v1beta1")
                sideEffects = "None"
                clientConfig {
                    caBundle = cert.encodeBase64()
                    service {
                        name = server.name
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
                        scope = it.scope
                    }
                )
                namespaceSelector = it.namespaceSelector
                failurePolicy = it.failurePolicy
            }
        }
    }


