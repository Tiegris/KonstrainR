package dsl

import io.github.serpro69.kfaker.faker

internal fun randomName(obj: Any): String {
    val faker = faker {
        fakerConfig {
            randomSeed = obj.hashCode().toLong()
        }
    }
    val first = faker.adjective.unique.positive().join()
    val second = faker.app.name().join()
    return "$first-$second"
}

internal fun String.join(): String {
    return this.split(" ").joinToString { it.replaceFirst(it.first(), it.first().uppercaseChar()) }
}
