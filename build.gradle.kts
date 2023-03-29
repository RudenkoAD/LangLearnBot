plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.langlearner"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.freQuensy23-coder:reverso-api:0.2")
    testImplementation(kotlin("test"))
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-support-postgresql:3.6.0")
    implementation ("dev.inmo:tgbotapi:7.0.0")
    implementation("org.postgresql:postgresql:42.3.8")
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