package me.btieger.dsl

import kotlinx.serialization.json.JsonObject

class WebhookBuilder(val defaults: WebhookConfigBundle) {
    private var _operations: Array<out Type> by setExactlyOnce(defaults.operations)
    private var _apiGroups: Array<out Type> by setExactlyOnce(defaults.apiGroups)
    private var _apiVersions: Array<out Type> by setExactlyOnce(defaults.apiVersion)
    private var _resources: Array<out Type> by setExactlyOnce(defaults.resources)
    private var _scope: Type by setExactlyOnce(defaults.scope ?: ANY)
    private var _namespaceSelector: NamespaceSelector by setExactlyOnce(defaults.namespaceSelector)
    private var _failurePolicy: FailurePolicy by setExactlyOnce(defaults.failurePolicy ?: FAIL)

    @DslMarkerVerb5
    var logRequest by setExactlyOnce(defaults.logRequest ?: false)
    @DslMarkerVerb5
    var logResponse by setExactlyOnce(defaults.logResponse ?: false)

    @DslMarkerVerb5
    var path: String by setExactlyOnce()

    @DslMarkerVerb5
    var behavior: (JsonObject) -> WebhookDecision by setExactlyOnce()

    @DslMarkerBlock
    fun withContext(setup: WebhookBehaviorBuilder.()->Unit): WebhookDecision {
        val provider = WebhookBehaviorBuilder().apply(setup)
        return provider.build()
    }

    private fun validateName(name: String): String {
        val name = name.split(' ', '.').joinToString(separator = "-")
        for (c in name) {
            if (!(c in 'A'..'Z' || c in 'a'..'z' || c == '-' || c == '_'))
            // TODO
                throw Exception()
        }
        return name
    }

    private fun validatePath(path: String): String {
        var path = path
        if (path.first() != '/')
            path = "/$path"
        path.trimEnd('/')
        for (c in path) {
            if (c !in 'a'..'z' && c != '/' && c != '-' && c != '_')// TODO
                throw Exception()
        }
        return path
    }


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

    internal fun build(name: String): Webhook {
        val operations = _operations.map {it.string}
        val apiGroups = _apiGroups.map {it.string}
        val apiVersion = _apiVersions.map {it.string}
        val resources = _resources.map {it.string}
        val scope = _scope.string
        val namespaceSelector = _namespaceSelector
        val failurePolicy = _failurePolicy
        val _name = validateName(name)
        val _path = validatePath(path)

        return Webhook(
            operations, apiGroups, apiVersion, resources, scope,
            namespaceSelector.selectorRule.rules, failurePolicy.string,
            _path, _name, behavior, logRequest, logResponse
        )
    }
}


abstract class Type(val string: String)

abstract class Operation(string: String) : Type(string)
@DslMarkerConstant
object CREATE : Operation("CREATE")
@DslMarkerConstant
object UPDATE : Operation("UPDATE")
@DslMarkerConstant
object DELETE : Operation("DELETE")
@DslMarkerConstant
object CONNECT : Operation("CONNECT")

abstract class ApiGroups(string: String) : Type(string)
@DslMarkerConstant
object CORE : ApiGroups("")

abstract class ApiVersions(string: String) : Type(string)
@DslMarkerConstant
object V1: ApiVersions("v1")
@DslMarkerConstant
object V1BETA1: ApiVersions("v1beta1")

abstract class Resources(string: String) : Type(string)
@DslMarkerConstant
object DEPLOYMENTS: Resources("deployments")
@DslMarkerConstant
object PODS: Resources("pods")
@DslMarkerConstant
object REPLICASETS: Resources("replicasets")

abstract class Scope(string: String) : Type(string)
@DslMarkerConstant
object CLUSTER: Scope("Clustered")
@DslMarkerConstant
object NAMESPACED: Scope("Namespaced")

abstract class FailurePolicy(string: String) : Type(string)
@DslMarkerConstant
object IGNORE : FailurePolicy("Ignore")
@DslMarkerConstant
object FAIL : FailurePolicy("Fail")

@DslMarkerConstant
object ANY : Type("*")
@DslMarkerConstant
class CUSTOM(string: String) : Type(string)

class Webhook (
    val operations: List<String>,
    val apiGroups: List<String>,
    val apiVersion: List<String>,
    val resources: List<String>,
    val scope: String,
    val namespaceSelector: Map<String, String>,
    val failurePolicy: String,
    val path: String,
    name: String,
    val provider: (JsonObject) -> WebhookDecision,
    val logRequest: Boolean,
    val logResponse: Boolean,
) : Component(name)