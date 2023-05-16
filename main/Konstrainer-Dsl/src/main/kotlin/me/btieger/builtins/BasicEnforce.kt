package me.btieger.builtins

import me.btieger.dsl.*

val defaults = webhookConfigBundle {
    apiGroups(CORE)
    apiVersions(ANY)
    resources(DEPLOYMENTS)
    namespaceSelector {
        matchLabels {
            "managed" eq "true"
        }
    }
    failurePolicy(FAIL)
    logRequest = true
    logResponse = true
}

val webhookServer = server("basic-webhook-rules") {
    webhook("cut-history", defaults) {
        operations(CREATE, UPDATE)
        behavior {
            val revisionHistoryLimit = (request jqx "/object/spec/revisionHistoryLimit" parseAs int) ?: 10
            warnings {
                if (revisionHistoryLimit > 4) warning("RevisionHistoryLimit was set to 4, from original: $revisionHistoryLimit")
            }
            patch {
                if (revisionHistoryLimit > 4) replace("spec/revisionHistoryLimit", "4")
            }
        }
    }
}