package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import me.btieger.services.helm.HelmService

fun HelmService.deployment(server: Server, agentId: Int) =
    Deployment().apply {
        metadata(server.name, config.namespace, agentId)
        spec {
            replicas = 1
            selector {
                matchLabels = myLabels(server.name, agentId)
            }
            template {
                metadata {
                    namespace = config.namespace
                    labels = myLabels(server.name, agentId)
                }
                spec {
                    serviceAccountName = server.name
                    containers = listOf(
                        newContainer {
                            name = "agent-server"
                            image = config.agentImage
                            imagePullPolicy = "Always"
                            env = listOf(
                                newEnvVar {
                                    name = "KSR_CORE_BASE_URL"
                                    value = config.serviceName
                                },
                                newEnvVar {
                                    name = "KSR_DSL_ID"
                                    value = agentId.toString()
                                },
                                newEnvVar {
                                    name = "KSR_AUTH_PASS"
                                    value = config.agentPass
                                },
                                newEnvVar {
                                    name = "KSR_AUTH_USER"
                                    value = config.agentUser
                                }
                            )
                            ports = listOf(
                                newContainerPort {
                                    containerPort = 8443
                                }
                            )
                            volumeMounts = listOf(
                                newVolumeMount {
                                    name = "tls-cert"
                                    mountPath = "/app/tls-cert"
                                    readOnly = true
                                }
                            )
                        }
                    )
                    volumes = listOf(
                        newVolume {
                            name = "tls-cert"
                            secret {
                                secretName = server.name
                            }
                        }
                    )
                }
            }
        }
    }
