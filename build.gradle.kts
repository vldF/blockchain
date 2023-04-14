import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("io.ktor.plugin") version "2.1.3"
}

group = "me.vldf.blockchain"
version = "1.0-SNAPSHOT"

val ktorVersion = "2.2.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("me.vldf.blockchain.MainKt")
}

ktor {
    fatJar {
        archivesName.set("blockchain.jar")
    }
}

val distZip by tasks
distZip.enabled = false

val distTar by tasks
distTar.enabled = false

val shadowDistTar by tasks
shadowDistTar.enabled = false

val shadowDistZip by tasks
shadowDistZip.enabled = false
