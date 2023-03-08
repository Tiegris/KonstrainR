val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val kubernetes_dsl_version: String by project
val kubernetes_client_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

group = "me.btieger"
version = "0.0.1-snapshot-230306-b"

ktor {
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
        localImageName.set("tiegris/konstrainer-core")
        imageTag.set("$version")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.fkorotkov:k8s-kotlin-dsl:${kubernetes_dsl_version}")
    implementation("io.fabric8:kubernetes-client:${kubernetes_client_version}")

    implementation("me.btieger:KonstrainerDsl:0.0.1-SNAPSHOT")

    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
    implementation("com.lordcodes.turtle:turtle:0.8.0")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    implementation("io.insert-koin:koin-ktor:$koin_version")

    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}