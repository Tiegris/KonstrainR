package me.btieger.dsl

import io.fabric8.kubernetes.api.model.LabelSelector

@DslMarkerBlock
fun webhookConfigBundle(setup: WebhookConfigBundleBuilder.() -> Unit): WebhookConfigBundle {
    val builder = WebhookConfigBundleBuilder().apply(setup)
    return builder.build()
}

class WebhookConfigBundleBuilder {
    private var _operations: Array<out String>? by setMaxOnce()
    private var _apiGroups: Array<out String>? by setMaxOnce()
    private var _apiVersions: Array<out String>? by setMaxOnce()
    private var _resources: Array<out String>? by setMaxOnce()
    private var _scope: String? by setMaxOnce(ANY)
    private var _namespaceSelector: LabelSelector? by setMaxOnce()
    private var _failurePolicy: String? by setMaxOnce(FAIL)

    @DslMarkerVerb5
    var logRequest by setExactlyOnce(false)
    @DslMarkerVerb5
    var logResponse by setExactlyOnce(false)

    @DslMarkerVerb5
    fun operations(vararg args: String) {
        _operations = args
    }

    @DslMarkerVerb5
    fun apiGroups(vararg args: String) {
        _apiGroups = args
    }

    @DslMarkerVerb5
    fun apiVersions(vararg args: String) {
        _apiVersions = args
    }

    @DslMarkerVerb5
    fun resources(vararg args: String) {
        _resources = args
    }

    @DslMarkerVerb5
    fun scope(scope: String) {
        _scope = scope
    }

    @DslMarkerBlock
    fun namespaceSelector(setup: LabelSelector.() -> Unit) {
        _namespaceSelector = LabelSelector().apply(setup)
    }

    @DslMarkerVerb5
    fun failurePolicy(failurePolicy: String) {
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
    val operations: Array<out String>?,
    val apiGroups: Array<out String>?,
    val apiVersion: Array<out String>?,
    val resources: Array<out String>?,
    val scope: String?,
    val namespaceSelector: LabelSelector?,
    val failurePolicy: String?,
    val logRequest: Boolean?,
    val logResponse: Boolean?,
)