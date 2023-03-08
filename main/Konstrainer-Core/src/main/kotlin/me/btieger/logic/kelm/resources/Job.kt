package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.batch.v1.*
import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.batch.v1.Job
import me.btieger.config

fun makeBuilderJob(dslId: Int, secret: String): Job {
    val _name = "dsl-build-job-$dslId"
    return Job().apply {
        metadata {
            name = _name
            namespace = config.namespace
            labels = mapOf(
                "app" to _name,
                "managedBy" to "konstrainer",
            )
        }
        spec {
            ttlSecondsAfterFinished = 60 * config.builderJobTtlMinutes
            backoffLimit = 1
            template {
                spec {
                    restartPolicy = "Never"
                    containers = listOf(
                        newContainer {
                            name = "builder"
                            imagePullPolicy = "Always"
                            image = config.builderImage
                            env = listOf(
                                newEnvVar {
                                    name = "KSR_SECRET"
                                    value = secret
                                },
                                newEnvVar {
                                    name = "KSR_CORE_BASE_URL"
                                    value = config.serviceName
                                },
                                newEnvVar {
                                    name = "KSR_DSL_ID"
                                    value = dslId.toString()
                                }
                            )
                        }
                    )
                }
            }

        }
    }
}

