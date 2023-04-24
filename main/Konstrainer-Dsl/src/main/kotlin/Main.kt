import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.btieger.dsl.*
import me.btieger.loader.Loader
import java.nio.file.Paths

fun main(args: Array<String>) {

    val x = Loader("me.btieger.example.ExampleKt").loadServer(Paths.get("C:\\Users\\btieger\\Documents\\KonstrainR\\main\\KonstraineR-Builder\\framework\\lib\\build\\libs\\lib.jar"))
    val k = x.rules[0].provider.invoke(JsonObject(mapOf()))

    println(x)
}