package me.btieger.services.cronjobs

import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.btieger.Config
import me.btieger.services.helm.resources.makeJobName
import me.btieger.persistance.DatabaseFactory
import me.btieger.persistance.tables.Dsl
import me.btieger.persistance.tables.Dsls
import me.btieger.persistance.tables.BuildStatus
import org.koin.ktor.ext.inject

import org.slf4j.LoggerFactory


fun Application.launchCleaner() {
    val k8sClient by inject<KubernetesClient>()
    val config by inject<Config>()
    launch {
        val interval = config.cleanerIntervalSeconds * 1000L
        val cleaner = Cleaner(k8sClient, config)
        while(true) {
            delay(interval)
            cleaner.runCleaner()
        }
    }
}

class Cleaner(private val kubectl: KubernetesClient, private val config: Config) {
    private val logger = LoggerFactory.getLogger("Cleaner")!!

    suspend fun runCleaner() = DatabaseFactory.dbQuery {
        logger.info("Running job cleaner")
        val inProgressDsls = Dsl.find { Dsls.buildStatus eq BuildStatus.Building }
        val k8sJobs = kubectl.batch().v1().jobs().inNamespace(config.namespace).list().items
        inProgressDsls.forEach { dsl ->
            val matchingJob = k8sJobs.find { job -> job.metadata.name == makeJobName(dsl.id.value) }
            if (matchingJob == null) {
                logger.warn("Dsl build with id: `{}` marked failed by cleaner", dsl.id.value)
                dsl.errorMessage = "Lost in space."
                dsl.buildStatus = BuildStatus.Failed
                dsl.jobSecret = null
            }
        }
        logger.info("Cleaning done")
    }

}