package me.btieger

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import me.btieger.controllers.apiVersion
import me.btieger.controllers.dslController
import me.btieger.controllers.echoController
import me.btieger.plugins.*

const val dslsUrlBase = "/api/$apiVersion/dsls"
class ApplicationTest {

    @Test
    fun testEcho() = testApplication {
        application {
            startup()
        }
        client.get("/echo").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("got it", bodyAsText())
        }
    }

    @Test
    fun testDslController() = testApplication {
        application {
            startup()
        }
        val response = client.get(dslsUrlBase)
        response.apply {
            this
        }
    }



}