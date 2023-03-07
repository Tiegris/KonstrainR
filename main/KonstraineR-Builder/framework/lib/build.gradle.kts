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
}
