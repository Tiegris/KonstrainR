import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
    kotlin("plugin.serialization") version "1.7.20"
    `maven-publish`
}

group = "me.btieger"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// gradle build publishToMavenLocal
publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.lordcodes.turtle:turtle:0.8.0")
    implementation("io.github.serpro69:kotlin-faker:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}
