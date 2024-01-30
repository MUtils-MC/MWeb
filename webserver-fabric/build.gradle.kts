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
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())

    val ktorVersion = property("ktorVersion")
    modImplementation("net.silkmc:silk-commands:1.10.3")
    modImplementation("net.silkmc:silk-core:1.10.3")
    modImplementation("net.silkmc:silk-nbt:1.10.3")
    modImplementation("net.fabricmc:fabric-loader:0.15.2")
    modImplementation(include("net.kyori:adventure-platform-fabric:5.10.1")!!)
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.91.3+1.20.4")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.16+kotlin.1.9.21")
    modImplementation(include("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT"))
    transitiveInclude(implementation("org.yaml:snakeyaml:2.2")!!)

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
