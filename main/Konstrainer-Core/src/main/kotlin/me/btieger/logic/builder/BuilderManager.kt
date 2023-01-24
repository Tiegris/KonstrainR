package me.btieger.logic.builder

import com.fkorotkov.kubernetes.apps.*
import com.fkorotkov.kubernetes.batch.v1.*
import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.batch.v1.Job
import me.btieger.logic.spawner.kelm.agentNamespace
import me.btieger.logic.spawner.kelm.jobsNamespace
import me.btieger.logic.spawner.kelm.labels

fun spawnBuilderJob(_name: String) {
    Job().apply{
        metadata {
            name = _name
            namespace = jobsNamespace
            labels = mapOf(
                "app" to _name,
                "managedBy" to "konstrainer",
            )
        }
        spec {
            ttlSecondsAfterFinished = 60*15 // 15 minutes
            backoffLimit = 2
            template {
                spec {
                    restartPolicy = "Never"
                    containers = listOf(
                        newContainer {
                            name = "builder"
                            image = "gradle:7.6-jdk17-jammy"
                            command = listOf("java", "-jar", "/app/builder.jar")
                        }
                    )
                }

            }

        }
    }
}

