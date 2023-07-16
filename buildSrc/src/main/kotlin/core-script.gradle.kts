plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")

    val ktorVersion = property("ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
//    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
//    implementation("io.ktor:ktor-client-core:$ktorVersion")
//    implementation("io.ktor:ktor-client-cio:$ktorVersion")
//    implementation("io.ktor:ktor-server-cors:$ktorVersion")
//    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
//    implementation("ch.qos.logback:logback-classic:$ktorVersion")

    implementation("org.yaml:snakeyaml:1.33")
    implementation("commons-codec:commons-codec:1.15")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(18)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "18"
    }
}
