package me.btieger.spawner.kelm

import io.fabric8.kubernetes.api.model.ServiceAccount

fun serviceAccount(values: Values) =
    ServiceAccount().apply {
        metadata(values)
    }

