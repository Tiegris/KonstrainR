plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {    
    implementation(files("/app/dsl.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}
