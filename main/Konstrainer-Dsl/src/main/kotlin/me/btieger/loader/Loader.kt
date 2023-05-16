package me.btieger.loader

import me.btieger.dsl.Server
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes
import kotlin.reflect.typeOf

class Loader(private val className: String) {

    fun loadServer(jarPath: String) = loadServer(URL(jarPath))

    fun loadServer(jarPath: Path) = loadServer(jarPath.toUri().toURL())

    fun loadServer(jarPath: URL): Server = URLClassLoader(
        arrayOf(jarPath),
        this.javaClass.classLoader
    ).use {
        val loadedClass = Class.forName(className, true, it)
        var getServerMethod: Method? = null
        for (item in loadedClass.methods) {
            if (item.returnType == Server::class.javaObjectType) {
                getServerMethod = item
                break
            }
        }
        if (getServerMethod != null)
            getServerMethod.invoke(null) as Server
        else
            throw Exception("Could not load server from file.")
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