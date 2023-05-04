import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    `maven-publish`
}

group = "me.btieger"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// ./gradlew build publishToMavenLocal
publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
