package rules

val ruleServer = server {
    whconf {
        // string will be added as constants
        operations = [CREATE, UPDATE]
        operations(CREATE, UPDATE, DELETE, CONNECT, ANY)
        apiGroups(ANY)
        apiVersions(ANY)
        resources(PODS, SERVICES, DEPLOYMENTS)
        scope(CLUSTERS, NAMESPACED, ANY)
        namespaceSelector {
            // for now only support MatchLabels
            matchLabels {
                "managed" eq "true"
                "" eq ""
            }
        }
        failurePolicy(FAIL)
    }
    rules {
        mutating rule("ruleName") {
            path = "/mutate" // Optional, default: generated from name
            allowed { // Optional, default: True
                /* Any Kotlin code, request can be accesed with the `it` keyword */
                it["metadata"]["labels"]["custom_label"] == "apples" // last expression will evaluated
            }
            warnings { // limited to total of 4096 char
                warning {
                    condition {
                        it["metadata"]["labels"]["app"].isNullOrEmpty()
                    }
                    message = "app label not set" // limited to 256 char
                }
            }
            status { // Optional, default: stock error message
                // Can be used to display error when allowed == False
                code = 403,
                message = "You cannot do this because it is Tuesday and your name starts with A"
            }
            patch {
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
        validation rule("ruleName") {
            // same as mutating rule, but no mutation
        }
    }


}