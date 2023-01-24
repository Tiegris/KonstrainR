package me.btieger.logic.loader

import me.btieger.dsl.Server
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

object Loader {
    private fun loadJar(myJar: Path): Server {
        val child = URLClassLoader(
            arrayOf(myJar.toUri().toURL()),
            this.javaClass.classLoader
        )
        val loadedClass = Class.forName("me.btieger.example.ExampleKt", true, child)
        val getServerMethod = loadedClass.getDeclaredMethod("getServer")
        return getServerMethod.invoke(null) as Server
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