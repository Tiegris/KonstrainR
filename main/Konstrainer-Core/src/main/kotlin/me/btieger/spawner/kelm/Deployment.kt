package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.*
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.apps.Deployment

fun Deployment.deployment(values: Values) =
    Deployment().apply {
        metadata(values)
        spec {
            replicas = 1
            template {
                metadata {
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
                                    valueFrom {
                                        fieldRef {
                                            fieldPath = "metadata.namespace"
                                        }
                                    }
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

