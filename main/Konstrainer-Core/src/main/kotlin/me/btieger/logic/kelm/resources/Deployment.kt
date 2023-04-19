package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import me.btieger.Config
import me.btieger.logic.kelm.HelmService

fun HelmService.deployment(server: Server) =
    Deployment().apply {
        metadata(server.whName, config.namespace)
        spec {
            replicas = 1
            selector {
                matchLabels = myLabels(server.whName)
            }
            template {
                metadata {
                    namespace = config.namespace
                    labels = myLabels(server.whName)
                }
                spec {
                    containers = listOf(
                        newContainer {
                            name = "agent-server"
                            image = config.agentImage
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
                                secretName = server.whName
                            }
                        }
                    )
                }
            }
        }
    }
