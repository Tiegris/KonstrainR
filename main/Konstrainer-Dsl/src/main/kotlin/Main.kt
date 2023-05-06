import kotlinx.serialization.json.*
import me.btieger.dsl.*

fun main(args: Array<String>) {

    val k = me.btieger.example.server

    val d = WebhookBehaviorBuilder(JsonObject(mapOf())).apply(k.webhooks.first().provider).build()

    println()

}