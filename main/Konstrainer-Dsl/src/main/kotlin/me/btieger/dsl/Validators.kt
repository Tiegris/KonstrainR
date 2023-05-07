package me.btieger.dsl

internal fun validateWhName(name: String): String {
    val name = name.split(' ', '.').joinToString(separator = "-")
    for (c in name) {
        if (!(c in 'A'..'Z' || c in 'a'..'z' || c == '-' || c == '_'))
        // TODO
            throw Exception()
    }
    return name
}

internal fun validatePath(path: String): String {
    var path = path
    if (path.first() != '/')
        path = "/$path"
    path.trimEnd('/')
    for (c in path) {
        if (c !in 'a'..'z' && c != '/' && c != '-' && c != '_')// TODO
            throw Exception()
    }
    return path
}