plugins {
    `core-script`
    `adventure-script`
}

dependencies {
    implementation(project(":api"))
    implementation("io.ktor:ktor-client-cio-jvm:2.3.7")
}

group = "de.miraculixx.mvanilla"
version = "1.0.0"
setProperty("module_name", "mvanilla")
