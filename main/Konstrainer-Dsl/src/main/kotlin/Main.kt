import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import me.btieger.dsl.*
fun main(args: Array<String>) {
    val s = "asd/asdf'/'asdfasdf/"

    val json = buildJsonObject {
        put("a", "asdf")
        putJsonArray("b") {
            add("x")
            add("y")
            add(5)
        }
    }

    val a = json jqx "/b/2" parseAs int
    println(a)
}