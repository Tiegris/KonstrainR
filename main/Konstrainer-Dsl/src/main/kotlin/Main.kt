import kotlinx.serialization.json.*
import me.btieger.dsl.*

fun main(args: Array<String>) {

    val k = me.btieger.example.server

    (k.webhooks[0] as Webhook).provider(JsonObject(mapOf()))

    println()

}