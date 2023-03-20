package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.batch.v1.*
import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.batch.v1.Job
import me.btieger.Config

fun makeJobName(dslId: Int) = "dsl-build-job-$dslId"

fun makeBuilderJob(dslId: Int, secret: String): Job {
    val _name = makeJobName(dslId)
    return Job().apply {
        metadata {
            name = _name
            namespace = Config.namespace
            labels = mapOf(
                "app" to _name,
                "managedBy" to "konstrainer",
            )
        }
        spec {
            ttlSecondsAfterFinished = 60 * Config.builderJobTtlMinutes
            backoffLimit = 1
            template {
                spec {
                    restartPolicy = "Never"
                    containers = listOf(
                        newContainer {
                            name = "builder"
                            imagePullPolicy = "Always"
                            image = Config.builderImage
                            env = listOf(
                                newEnvVar {
                                    name = "KSR_SECRET"
                                    value = secret
                                },
                                newEnvVar {
                                    name = "KSR_CORE_BASE_URL"
                                    value = Config.serviceName
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

