package me.btieger

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import me.btieger.controllers.apiVersion

class ApplicationTest : KonstrainerTestBase() {
    val dslsUrlBase = "/api/$apiVersion/dsls"

    @Test
    fun testEcho() = konstrainerTest {
        client.get("/echo").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("got it", bodyAsText())
        }
    }

    @Test
    fun testDslController() = konstrainerTest {
        val response = client.get(dslsUrlBase)
        response.apply {
            this
        }
    }



}