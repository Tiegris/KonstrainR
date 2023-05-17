package me.btieger.builtins

import me.btieger.dsl.*

val loggerDefaults = webhookConfigBundle {
    namespaceSelector {
        matchLabels {
            "managed" eq "true"
        }
    }
    failurePolicy(FAIL)
    logRequest = true
    logResponse = true
    operations(CREATE, UPDATE)
}

val loggingServer = server("basic-webhook-rules") {

    webhook("log-deployments") {
        apiGroups(APPS, CORE)
        apiVersions(ANY)
        resources(DEPLOYMENTS, PODS)
        behavior {

        }
    }

}