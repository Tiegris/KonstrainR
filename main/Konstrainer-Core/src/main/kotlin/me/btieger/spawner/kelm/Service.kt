package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.Service

fun Service.service(serviceName: String) =
    Service().apply {
        metadata(serviceName)

    }

