plugins {
    `core-script`
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "JitPack"
        setUrl("https://jitpack.io")
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())

    val ktorVersion = property("ktorVersion")
    val silkVersion = "1.9.8"
    modImplementation("net.silkmc:silk-commands:$silkVersion")
    modImplementation("net.silkmc:silk-core:$silkVersion")
    modImplementation("net.silkmc:silk-nbt:$silkVersion")
    modImplementation("net.silkmc:silk-persistence:$silkVersion")
    modImplementation("net.fabricmc:fabric-loader:0.14.21")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.83.1+1.20.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.5+kotlin.1.8.22")
    modImplementation(include("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT"))
    modImplementation(include("net.kyori:adventure-platform-fabric:5.10.0-SNAPSHOT")!!)
    modImplementation(include("org.yaml:snakeyaml:1.33")!!)

    transitiveInclude("io.ktor:ktor-server-core-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-server-cio:$ktorVersion")

    implementation(include(project(":vanilla"))!!)
    implementation(include(project(":api"))!!)

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xskip-prerelease-check"
        }
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("net.silkmc.silk.core.annotations.ExperimentalSilkApi")
        }
    }
}

sourceSets {
    main {
        resources.srcDirs("$rootDir/commons/")
    }
}
