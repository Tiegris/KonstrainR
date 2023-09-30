package me.btieger.dsl

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.DaemonSet
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.batch.v1.CronJob
import io.fabric8.kubernetes.api.model.batch.v1.Job
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.utils.Serialization
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class WebhookDecision(val allowed: Boolean, val patch: JsonArray?, val warnings: List<String>?, val status: Status)
class WebhookBehaviorBuilder(private val _request: JsonObject, private val _kubectl: KubernetesClient) {
    private var _allowed: Boolean by setExactlyOnce(true)
    private var _warnings: List<String>? by setMaxOnce()
    private var _patch: JsonArray? by setMaxOnce()
    private var _status: Status by setExactlyOnce(StatusBuilder().build())

    @DslMarkerConstant
    val kubectl
        get() = _kubectl

    @DslMarkerConstant
    val request
        get() = _request

    @DslMarkerConstant
    val currentObject by lazy {
        autoUnmarshal("object")
    }

    @DslMarkerConstant
    val oldObject by lazy {
        autoUnmarshal("oldObject")
    }

    @DslMarkerConstant
    val podSpec: PodSpec? by lazy {
        unmarshal(request jqx "/object/spec/template/spec")
    }

    @DslMarkerVerb5
    inline fun <reified T> unmarshal(json: JsonElement) : T? {
        val strObject = json.toString()
        return try {
            Serialization.unmarshal(strObject, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun autoUnmarshal(selector: String): HasMetadata? {
        val kind = request jqx "/$selector/kind" parseAs string
        val unmarshallerClazzMap = mapOf(
            "Deployment" to Deployment::class.java,
            "Service" to Service::class.java,
            "Pod" to Pod::class.java,
            "Secret" to Secret::class.java,
            "DaemonSet" to DaemonSet::class.java,
            "StatefulSet" to StatefulSet::class.java,
            "Job" to Job::class.java,
            "CronJob" to CronJob::class.java,
            "Namespace" to Namespace::class.java,
        )
        return try {
            val strObject = (request jqx selector).toString()
            Serialization.unmarshal(strObject, unmarshallerClazzMap[kind])
        } catch(e: Exception) {
            null
        }
    }

    @DslMarkerBlock
    fun allowed(script: () -> Boolean) {
        _allowed = script()
    }

    @DslMarkerBlock
    fun patch(setup: PatchBuilder.() -> Unit) {
        val builder = PatchBuilder().apply(setup)
        _patch = builder.build()
    }

    @DslMarkerBlock
    fun warnings(setup: WarningsBuilder.() -> Unit) {
        val builder = WarningsBuilder().apply(setup)
        _warnings = builder.build()
    }

    @DslMarkerBlock
    fun status(setup: StatusBuilder.() -> Unit) {
        val builder = StatusBuilder().apply(setup)
        _status = builder.build()
    }

    fun build() = WebhookDecision(_allowed, _patch, _warnings, _status)

}



