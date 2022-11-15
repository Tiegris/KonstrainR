package me.btieger.dsl

import kotlinx.serialization.json.JsonObject

class PatchBuilder {
    private val _patches = mutableListOf<PatchProvider>()

    fun add(path: String, value: String) {

    }

    fun remove(path: String) {

    }

    fun replace(path: String, value: String) {

    }

    fun copy(from: String, to: String) {

    }

    fun move(from: String, to: String) {

    }

    fun test(path: String, value: String) {

    }

}

interface PatchProvider {
    val patch : JsonObject
}

class Patches