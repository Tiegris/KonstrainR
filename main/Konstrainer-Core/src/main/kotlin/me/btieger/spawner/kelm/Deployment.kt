package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import io.fabric8.kubernetes.api.model.apps.Deployment

fun deployment(values: Values) =
    Deployment().apply {
        metadata(values)
        spec {
            replicas = 1
            template {
                metadata {
                    namespace = agentNamespace
                    labels(values)
                }
                spec {
                    containers = listOf(
                        newContainer {
                            name = values.name
                            image = values.image
                            env = listOf(
                                newEnvVar {
                                    name = "SERVICE_NAME"
                                    value = values.name
                                },
                                newEnvVar {
                                    name = "POD_NAMESPACE"
                                    value = agentNamespace
                                },
                            )
                            lifecycle {
                                preStop {
                                    exec {
                                        command = listOf("/bin/sh", "-c", "/prestop.sh")
                                    }
                                }
                            }
                            ports = listOf(
                                newContainerPort {
                                    containerPort = 8443
                                }
                            )
                        }
                    )
                }
            }
        }
    }

