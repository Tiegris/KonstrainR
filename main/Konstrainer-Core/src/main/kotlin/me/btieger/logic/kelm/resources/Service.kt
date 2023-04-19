package me.btieger.logic.kelm.resources

import com.fkorotkov.kubernetes.newServicePort
import com.fkorotkov.kubernetes.spec
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.Service
import me.btieger.logic.kelm.HelmService

fun HelmService.service(server: Server, agentId: Int) =
    Service().apply {
        metadata(server.whName, config.namespace, agentId)
        spec {
            ports = listOf(
                newServicePort {
                    port = 443
                    targetPort = IntOrString(8443)
                }
            )
            selector = myLabels(server.whName, agentId)
        }
    }

