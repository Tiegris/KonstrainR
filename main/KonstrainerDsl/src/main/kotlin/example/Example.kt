package example

import dsl.*

val server = server {
    whconf {
        // I will probalby change the syntax to an assignment like
        // operations = CREATE + UPDATE + DELETE
        operations(CREATE, UPDATE, DELETE, CONNECT)
        apiGroups(CORE)
        apiVersions(ANY)
        resources(PODS, DEPLOYMENTS, REPLICASETS)
        scope(ANY)
        namespaceSelector { // This syntax is far from final
            // for now only support MatchLabels
            matchLabels {
                "managed" eq "true"
                "istio-injectio" eq "false"
                // not present ???
            }
        }
        failurePolicy(FAIL)
    }
    rules {
        mutating rule {
            name = "valami" // Optional, default: deterministic random generated
            path = "/mutate" // Optional, default: generated from name
            allowed { // Optional, default: True
                /* Any Kotlin code, request can be accesed with the `it` keyword */
                //it["metadata"]["labels"]["custom_label"] == "apples" // last expression will evaluated
                true
            }
            warnings { // limited to total of 4096 char
                warning {
                    condition {
                        // notFoundPolicy ???
                        it["metadata"]["labels"]["app"].isNullOrEmpty()
                    }
                    message = "app label not set" // limited to 256 char
                }
                warning("app label not set") given {
                    it["metadata"]["labels"]["app"].isNullOrEmpty()
                }
            }
            status { // Optional, default: stock error message
                // Can be used to display error when allowed == False
                code = 403
                message = "You cannot do this because it is Tuesday and your name starts with A"
            }
            patch {
                // value can be object too
                add("path", "value")
                add new "value" at "path"

                remove("path")
                remove at "path"

                replace("path", "value")
                replace at "path" with "value"

                copy("from", "to")
                copy from "path_from" to "path_to"

                move("from", "to")
                move from "path_from" to "path_to"

                test("path", "value")
                test that "path" eq "value"
            }
        }
        validation rule {
            // same as mutating rule, but no mutation
        }
    }
}
