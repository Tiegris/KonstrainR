package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.newServicePort
import com.fkorotkov.kubernetes.spec
import me.btieger.dsl.*
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.Service

fun service(server: Server) =
    Service().apply {
        metadata(server.whName)
        spec {
            ports = listOf(
                newServicePort {
                    port = 443
                    targetPort = IntOrString(8443)
                }
            )
            selector = mapOf(
               "app" to ""
            )
        }
    }

