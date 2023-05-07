package me.btieger.services.helm.resources

import com.fkorotkov.kubernetes.batch.v1.*
import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.batch.v1.Job
import me.btieger.services.helm.HelmService

fun makeJobName(dslId: Int) = "dsl-build-job-$dslId"

fun HelmService.makeBuilderJob(dslId: Int, secret: String): Job {
    val _name = makeJobName(dslId)
    return Job().apply {
        metadata {
            name = _name
            namespace = config.namespace
            labels = myLabels(_name, dslId)
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

