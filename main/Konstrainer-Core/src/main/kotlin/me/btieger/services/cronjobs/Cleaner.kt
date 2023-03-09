package me.btieger.services.cronjobs

import io.fabric8.kubernetes.client.KubernetesClient
import me.btieger.config
import me.btieger.logic.kelm.resources.makeJobName
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.Status

import org.slf4j.LoggerFactory

class Cleaner(private val kubectl: KubernetesClient) {
    private val logger = LoggerFactory.getLogger("Cleaner")!!

    suspend fun runCleaner() = DatabaseFactory.dbQuery {
        logger.info("Running job cleaner")
        val inProgressDsls = Dsl.find { Dsls.status eq Status.Building }
        val k8sJobs = kubectl.batch().v1().jobs().inNamespace(config.namespace).list().items
        inProgressDsls.forEach { dsl ->
            val matchingJob = k8sJobs.find { job -> job.metadata.name == makeJobName(dsl.id.value) }
            if (matchingJob == null) {
                logger.warn("Dsl build with id: `{}` marked failed by cleaner", dsl.id.value)
                dsl.errorMessage = "Lost in space."
                dsl.status = Status.Failed
                dsl.jobSecret = null
            }
        }
    }

}