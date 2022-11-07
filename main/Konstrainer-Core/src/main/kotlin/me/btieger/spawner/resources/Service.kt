package me.btieger.spawner.resources

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.*
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment

fun Service.service(serviceName: String) =
    Service().apply {

    }

