plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.test"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.freQuensy23-coder:reverso-api:1c0c2e14d4")
    testImplementation(kotlin("test"))
    implementation ("dev.inmo:tgbotapi:7.0.0")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("org.slf4j:slf4j-simple:2.0.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}