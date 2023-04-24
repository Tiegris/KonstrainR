import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.btieger.dsl.*
import me.btieger.loader.Loader
import java.nio.file.Paths

fun main(args: Array<String>) {

    //val x = Json.encodeToString(me.btieger.example.server)
    val x = Loader("me.btieger.example.ExampleKt").loadServer(Paths.get("C:\\Users\\btieger\\Documents\\KonstrainR\\main\\Konstrainer-Dsl\\build\\libs\\KonstrainerDsl-0.0.1-SNAPSHOT.jar"))

    val k = x.rules[0].provider.invoke(JsonObject(mapOf()))

    println(x)
}