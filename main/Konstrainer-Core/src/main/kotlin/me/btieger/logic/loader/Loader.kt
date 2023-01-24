package me.btieger.logic.loader

import me.btieger.dsl.Server
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

object Loader {
    private fun loadJar(myJar: Path): Server {
        val child = URLClassLoader(
            arrayOf(myJar.toUri().toURL()),
            this.javaClass.classLoader
        )
        val classToLoad = Class.forName("me.btieger.example.ExampleKt", true, child)
        val method = classToLoad.getDeclaredMethod("getServer")
        return method.invoke(null) as Server
    }

    fun loadServerConfig(jar: ByteArray): Server {
        var path: Path? = null
        try {
            path = Files.createTempFile("server", ".jar")
            path.writeBytes(jar)
            return loadJar(path)
        } finally {
            path?.deleteIfExists()
        }
    }

}