package runner

import com.lordcodes.turtle.shellRun
import dsl.Server
import kotlinx.serialization.json.JsonObject

fun Server.runSetup() {
    val output = shellRun("/setup", listOf(
        "", "",
        "", "",
        "", "",
    ))

    TODO()
}

fun Server.execute(request: JsonObject): JsonObject {
    TODO()
}