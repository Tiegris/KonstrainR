package me.btieger.dsl

import io.fabric8.kubernetes.api.model.LabelSelector

typealias WebhookBehaviorProvider = WebhookBehaviorBuilder.()->Unit
class WebhookBuilder(defaults: WebhookConfigBundle) {
    private var _operations: Array<out String> by setExactlyOnce(defaults.operations)
    private var _apiGroups: Array<out String> by setExactlyOnce(defaults.apiGroups)
    private var _apiVersions: Array<out String> by setExactlyOnce(defaults.apiVersion)
    private var _resources: Array<out String> by setExactlyOnce(defaults.resources)
    private var _scope: String by setExactlyOnce(defaults.scope ?: ANY)
    private var _namespaceSelector: LabelSelector by setExactlyOnce(defaults.namespaceSelector)
    private var _failurePolicy: String by setExactlyOnce(defaults.failurePolicy ?: FAIL)

    @DslMarkerVerb5
    var logRequest by setExactlyOnce(defaults.logRequest ?: false)
    @DslMarkerVerb5
    var logResponse by setExactlyOnce(defaults.logResponse ?: false)

    private var behavior: WebhookBehaviorProvider by setExactlyOnce()

    @DslMarkerBlock
    fun behavior(setup: WebhookBehaviorProvider) {
        behavior = setup
    }

    @DslMarkerVerb5
    fun operations(vararg args: String) {
        _operations = args
    }

    @DslMarkerVerb5
    fun apiGroups(vararg args: String) {
        _apiGroups = args
    }

    @DslMarkerBlock
    fun namespaceSelector(setup: LabelSelector.() -> Unit) {
        _namespaceSelector = LabelSelector().apply(setup)
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

    @DslMarkerVerb5
    fun failurePolicy(failurePolicy: String) {
        _failurePolicy = failurePolicy
    }

    internal fun build(name: String): Webhook {
        val operations = _operations.map {it}
        val apiGroups = _apiGroups.map {it}
        val apiVersion = _apiVersions.map {it}
        val resources = _resources.map {it}
        val scope = _scope
        val namespaceSelector = _namespaceSelector
        val failurePolicy = _failurePolicy
        val _name = validateWhName(name)
        val _path = validatePath(name)

        return Webhook(
            operations, apiGroups, apiVersion, resources, scope,
            namespaceSelector, failurePolicy,
            _path, _name, behavior, logRequest, logResponse
        )
    }
}


@DslMarkerConstant
val CREATE = "CREATE"
@DslMarkerConstant
val UPDATE = "UPDATE"
@DslMarkerConstant
val DELETE = "DELETE"
@DslMarkerConstant
val CONNECT = "CONNECT"

@DslMarkerConstant
val CORE = ""
@DslMarkerConstant
val APPS = "apps"

@DslMarkerConstant
val DEPLOYMENTS = "deployments"
@DslMarkerConstant
val PODS = "pods"
@DslMarkerConstant
val REPLICASETS = "replicasets"
@DslMarkerConstant
val STATEFULSETS = "statefulsets"
@DslMarkerConstant
val DAEMONSETS = "daemonsets"

@DslMarkerConstant
val CLUSTER = "Clustered"
@DslMarkerConstant
val NAMESPACED = "Namespaced"

@DslMarkerConstant
val IGNORE ="Ignore"
@DslMarkerConstant
val FAIL = "Fail"

@DslMarkerConstant
val ANY = "*"


class Webhook (
    val operations: List<String>,
    val apiGroups: List<String>,
    val apiVersion: List<String>,
    val resources: List<String>,
    val scope: String,
    val namespaceSelector: LabelSelector,
    val failurePolicy: String,
    val path: String,
    val name: String,
    val provider: WebhookBehaviorProvider,
    val logRequest: Boolean,
    val logResponse: Boolean,
)