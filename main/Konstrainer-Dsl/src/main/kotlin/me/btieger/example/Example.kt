package me.btieger.example

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.btieger.dsl.*

val server = server {
    whName = "example"
    serverBaseImage = ""
    whconf {
        // I will probalby change the syntax to an assignment like
        //boperations = CREATE + UPDATE + DELETE
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
        rule {
            name = "valami" // Optional, default: deterministic random generated
            path = "/mutate" // Optional, default: generated from name

            behavior = fun (context) = withContext {
                allowed { // Optional, default: True
                    /* Any Kotlin code, request can be accesed with the `it` keyword */
                    //it["metadata"]["labels"]["custom_label"] == "apples" // last expression will evaluated
                    context["object"]?.jsonObject?.get("kind")?.jsonPrimitive?.content == ""
                }
                status { // Optional, default: stock error message
                    // Can be used to display error when (allowed == False)
                    code = 403
                    message = "You cannot do this because it is Tuesday and your name starts with A"
                }
                patch {
                    add("/metadata/labels/alma", context["object"]?.jsonObject?.get("kind")?.jsonPrimitive?.content ?: "nil")
                    // payload should be accesible here

                    //context["apiVersion"]?.jsonPrimitive?.content

                    // value can be object too
//                remove("path")
//                replace("path", "value")
//                copy("from", "to")
//                move("from", "to")
//                test("path", "value")
                }
            }

//            precondition {
//
//            }


//            warnings { // limited to total of 4096 char
///*                warning {
//                    condition {
//                        // notFoundPolicy ???
//                        it["metadata"]["labels"]["app"].isNullOrEmpty()
//                    }
//                    message = "app label not set" // limited to 256 char
//                }
//                warning("app label not set") condition {
//                    it["metadata"]["labels"]["app"].isNullOrEmpty()
//                }*/
//            }


        }


    }

}
