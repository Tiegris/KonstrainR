

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
    loop {
        type = "deployment"
        val resources = k8s.get().deployments()
        onEach(resources) {
            predicate {
                resource.hasNoResourcesLimit()
            }
            message = "${resource.name} is bad!"
        }
    }
}