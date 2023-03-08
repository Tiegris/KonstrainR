package me.btieger

import io.ktor.client.call.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.persistance.DatabaseFactory
import me.btieger.plugins.*

class ApplicationTest {

    @Test
    fun testEcho() = testApplication {
        application {
            configureSerialization()
            echoController()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("got it", bodyAsText())
        }
    }

//    @Test
//    fun testEmtpy() = testApplication {
//        application {
//            module()
//        }
//        client.get("/api/v1/dsls").apply {
//            assertEquals(HttpStatusCode.OK, status)
//
//            assertEquals("[]", bodyAsText())
//        }
//    }

}