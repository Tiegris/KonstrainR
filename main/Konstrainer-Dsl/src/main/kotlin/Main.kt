import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.btieger.dsl.*
import me.btieger.loader.Loader
import java.nio.file.Paths

fun main(args: Array<String>) {

    val k = me.btieger.example.server

    (k.components[0] as Webhook).provider(JsonObject(mapOf()))

    println()

}