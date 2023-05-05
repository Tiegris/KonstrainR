

val server = warningServer {
    permissions {
        rule {
            apiGroups()
            resources()
            verbs()
        }
        rule {
            apiGroups()
            resources()
            verbs()
        }
    }
    watch {
        group = "deployments"
        val resources = k8s.get().deployments()
        onEach(resources) {
            predicate {
                resource.hasNoResourcesLimit()
            }
            mark(YELLOW)
            message = "${resource.name} is bad!"
        }
    }
}