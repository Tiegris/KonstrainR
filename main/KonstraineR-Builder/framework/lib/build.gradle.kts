plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(files("/app/libs/KonstrainerDsl-0.0.1-SNAPSHOT.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}
