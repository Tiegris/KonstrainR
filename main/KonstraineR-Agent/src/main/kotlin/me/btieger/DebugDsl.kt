package me.btieger

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
}

val debugServer = server("example-server") {

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

}
