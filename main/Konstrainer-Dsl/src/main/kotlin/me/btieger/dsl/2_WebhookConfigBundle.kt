package me.btieger.dsl

@DslMarkerBlock
fun webhookConfigBundle(setup: WebhookConfigBundleBuilder.() -> Unit): WebhookConfigBundle {
    val builder = WebhookConfigBundleBuilder().apply(setup)
    return builder.build()
}

class WebhookConfigBundleBuilder {
    private var _operations: Array<out Type>? by setMaxOnce()
    private var _apiGroups: Array<out Type>? by setMaxOnce()
    private var _apiVersions: Array<out Type>? by setMaxOnce()
    private var _resources: Array<out Type>? by setMaxOnce()
    private var _scope: Type? by setMaxOnce(ANY)
    private var _namespaceSelector: NamespaceSelector? by setMaxOnce()
    private var _failurePolicy: FailurePolicy? by setMaxOnce(FAIL)

    @DslMarkerVerb5
    var logRequest by setExactlyOnce(false)
    @DslMarkerVerb5
    var logResponse by setExactlyOnce(false)

    @DslMarkerVerb5
    fun operations(vararg args: Operation) {
        _operations = args
    }
    @DslMarkerVerb5
    fun operations(args: ANY) {
        _operations = arrayOf(ANY)
    }

    @DslMarkerVerb5
    fun apiGroups(vararg args: ApiGroups) {
        _apiGroups = args
    }
    @DslMarkerVerb5
    fun apiGroups(args: ANY) {
        _apiGroups = arrayOf(ANY)
    }

    @DslMarkerVerb5
    fun apiVersions(vararg args: ApiVersions) {
        _apiVersions = args
    }
    @DslMarkerVerb5
    fun apiVersions(args: ANY) {
        _apiVersions = arrayOf(ANY)
    }

    @DslMarkerVerb5
    fun resources(vararg args: Resources) {
        _resources = args
    }
    @DslMarkerVerb5
    fun resources(args: ANY) {
        _resources = arrayOf(ANY)
    }

    @DslMarkerVerb5
    fun scope(scope: Scope) {
        _scope = scope
    }
    @DslMarkerVerb5
    fun scope(scope: ANY) {
        _scope = ANY
    }

    @DslMarkerBlock
    fun namespaceSelector(setup: NamespaceSelectorBuilder.() -> Unit) {
        val builder = NamespaceSelectorBuilder()
        builder.setup()
        _namespaceSelector = builder.build()
    }

    @DslMarkerVerb5
    fun failurePolicy(failurePolicy: FailurePolicy) {
        _failurePolicy = failurePolicy
    }

    internal fun build(): WebhookConfigBundle {
        return WebhookConfigBundle(
            _operations, _apiGroups, _apiVersions, _resources, _scope,
            _namespaceSelector, _failurePolicy,
            logRequest, logResponse
        )
    }
}

class WebhookConfigBundle(
    val operations: Array<out Type>?,
    val apiGroups: Array<out Type>?,
    val apiVersion: Array<out Type>?,
    val resources: Array<out Type>?,
    val scope: Type?,
    val namespaceSelector: NamespaceSelector?,
    val failurePolicy: FailurePolicy?,
    val logRequest: Boolean?,
    val logResponse: Boolean?,
)