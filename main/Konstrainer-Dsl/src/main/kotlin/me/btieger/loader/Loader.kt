package me.btieger.loader

import me.btieger.dsl.Server
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

class Loader(private val className: String, private val fieldGetterName: String = "getServer") {

    fun loadServer(jarPath: String) = loadServer(URL(jarPath))

    fun loadServer(jarPath: Path) = loadServer(jarPath.toUri().toURL())

    fun loadServer(jarPath: URL): Server = URLClassLoader(
        arrayOf(jarPath),
        this.javaClass.classLoader
    ).use {
        val loadedClass = Class.forName(className, true, it)
        val getServerMethod = loadedClass.getDeclaredMethod(fieldGetterName)
        getServerMethod.invoke(null) as Server
    }

    fun loadServer(jarBytes: ByteArray): Server {
        var path: Path? = null
        return try {
            path = Files.createTempFile("server", ".jar")
            path.writeBytes(jarBytes)
            loadServer(path)
        } finally {
            path?.deleteIfExists()
        }
    }

}