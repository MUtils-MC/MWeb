plugins {
    `core-script`
    `adventure-script`
}

dependencies {
    implementation(project(":api"))
}

group = "de.miraculixx.mvanilla"
version = "1.0.0"
setProperty("module_name", "mvanilla")
