package me.btieger.builtins

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import me.btieger.dsl.*

val defaults = webhookConfigBundle {
    operations(CREATE, UPDATE)
    apiGroups(APPS)
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
        behavior {
            val revisionHistoryLimit = (request jqx "/object/spec/revisionHistoryLimit" parseAs int) ?: 10
            warnings {
                if (revisionHistoryLimit > 4) warning("RevisionHistoryLimit was set to 4, from original: $revisionHistoryLimit")
            }
            patch {
                if (revisionHistoryLimit > 4) replace("/spec/revisionHistoryLimit", 4)
            }
        }
    }
    webhook("deny-no-resources", defaults) {
        behavior {
            val containers = (request jqx "/object/spec/template/spec/containers").jsonArray
            allowed {
                containers.all { (it.jsonObject jqx "/resources/").jsonObject.isNotEmpty() }
            }
            status {
                message = "Deployment must have resource definitions!"
            }
        }
    }
    webhook("warn-default-ns", defaults) {
        
    }

}