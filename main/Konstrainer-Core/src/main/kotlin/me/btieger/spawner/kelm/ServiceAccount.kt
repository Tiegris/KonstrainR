package me.btieger.spawner.kelm

import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.ServiceAccount

fun ServiceAccount.serviceAccount(serviceName: String) =
    ServiceAccount().apply {
        metadata(serviceName)

    }

