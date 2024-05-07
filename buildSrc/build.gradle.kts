plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.8.22"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    fun pluginDep(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"
    val kotlinVersion = "1.9.22"

    compileOnly(kotlin("gradle-plugin", kotlinVersion))
    runtimeOnly(kotlin("gradle-plugin", kotlinVersion))
    compileOnly(pluginDep("org.jetbrains.kotlin.plugin.serialization", kotlinVersion))
    runtimeOnly(pluginDep("org.jetbrains.kotlin.plugin.serialization", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // Fabric implementation
    implementation("net.fabricmc:fabric-loom:1.6-SNAPSHOT")
    implementation(pluginDep("io.github.juuxel.loom-quiltflower", "1.9.0"))

    // Paper implementation
    implementation(pluginDep("io.papermc.paperweight.userdev", "1.7.0"))
    implementation(pluginDep("xyz.jpenilla.run-paper", "2.2.4"))

    implementation(pluginDep("com.github.johnrengelman.shadow", "8.1.1"))
    implementation(pluginDep("com.modrinth.minotaur", "2.+"))
}