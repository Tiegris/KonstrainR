package me.btieger

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import me.btieger.dsl.Rule
import me.btieger.dsl.Server
import me.btieger.dsl.Status
import me.btieger.dsl.WhConf
import me.btieger.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val srv = Server("asd", "asd", listOf(
            Rule("R1", "/a", Status(200,"OK"), listOf()),
            Rule("R2", "/b", Status(200,"OK"), listOf()),
        ), WhConf(listOf(), listOf(), listOf(), listOf(), "", mapOf(), "")
        )

        application {
            configureRouting(srv)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}