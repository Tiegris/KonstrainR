package me.btieger.commonLibrary

fun String.toScreamingCamelCase() = StringBuilder().apply {
    for (c in this@toScreamingCamelCase) {
        if (c.isUpperCase()) {
            append('_')
            append(c)
        } else {
            append(c.uppercaseChar())
        }
    }
}.toString()

