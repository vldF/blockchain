plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
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


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
