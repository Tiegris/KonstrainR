package dsl

class WhConfBuilder {
    private var _operations: Array<out Type> by setOnce()
    private var _apiGroups: Array<out Type> by setOnce()
    private var _apiVersions: Array<out Type> by setOnce()
    private var _resources: Array<out Type> by setOnce()
    private var _scope: Type by setOnce(ANY)
    private var _namespaceSelector: NamespaceSelector by setOnce()
    private var _failurePolicy: FailurePolicy by setOnce(FAIL)

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

    internal fun build(): WhConf {
        val operations = _operations.map {it.string}
        val apiGroups = _apiGroups.map {it.string}
        val apiVersion = _apiVersions.map {it.string}
        val resources = _resources.map {it.string}
        val scope = _scope.string
        val namespaceSelector = _namespaceSelector
        val failurePolicy = _failurePolicy

        return WhConf(
            operations, apiGroups, apiVersion, resources, scope, namespaceSelector, failurePolicy
        )
    }
}


abstract class Type(val string: String)

abstract class Operation(string: String) : Type(string)
@DslMarkerConstant object CREATE : Operation("CREATE")
@DslMarkerConstant object UPDATE : Operation("UPDATE")
@DslMarkerConstant object DELETE : Operation("DELETE")
@DslMarkerConstant object CONNECT : Operation("CONNECT")

abstract class ApiGroups(string: String) : Type(string)
@DslMarkerConstant object CORE : ApiGroups("")

abstract class ApiVersions(string: String) : Type(string)
@DslMarkerConstant object V1: ApiVersions("v1")
@DslMarkerConstant object V1BETA1: ApiVersions("v1beta1")

abstract class Resources(string: String) : Type(string)
@DslMarkerConstant object DEPLOYMENTS: Resources("deployments")
@DslMarkerConstant object PODS: Resources("pods")
@DslMarkerConstant object REPLICASETS: Resources("replicasets")

abstract class Scope(string: String) : Type(string)
@DslMarkerConstant object CLUSTER: Scope("Clustered")
@DslMarkerConstant object NAMESPACED: Scope("Namespaced")

abstract class FailurePolicy(string: String) : Type(string)
@DslMarkerConstant object IGNORE : FailurePolicy("Ignore")
@DslMarkerConstant object FAIL : FailurePolicy("Fail")

@DslMarkerConstant object ANY : Type("*")
@DslMarkerConstant class CUSTOM(string: String) : Type(string)

class WhConf(
    val operations: List<String>,
    val apiGroups: List<String>,
    val apiVersion: List<String>,
    val resources: List<String>,
    val scope: String,
    val namespaceSelector: NamespaceSelector,
    val failurePolicy: FailurePolicy,
)
