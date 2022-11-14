package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.apps.Deployment

fun deployment(server: Server) =
    Deployment().apply {
        metadata(server.whName)
        spec {
            replicas = 1
            template {
                metadata {
                    namespace = agentNamespace
                    labels(server.whName)
                }
                spec {
                    containers = listOf(
                        newContainer {
                            name = "wh-server"
                            image = server.serverBaseImage
                            ports = listOf(
                                newContainerPort {
                                    containerPort = 8443
                                }
                            )
                            volumeMounts = listOf(
                                newVolumeMount {
                                    name = "config"
                                    mountPath = "/conf"
                                    readOnly = true
                                }
                            )
                        }
                    )
                    volumes = listOf(
                        newVolume {
                            name = "config"
                            configMap {
                                name = "${server.whName}-cm"
                                items = listOf(
                                    newKeyToPath {
                                        key = "keystore.jks"
                                        path = "keystore.jks"
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
