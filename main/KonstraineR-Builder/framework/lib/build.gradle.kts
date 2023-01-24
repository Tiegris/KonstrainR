plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {    
    implementation(files("/app/dsl.jar"))
}
