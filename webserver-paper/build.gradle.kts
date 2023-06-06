plugins {
    `core-script`
    `shadow-script`
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

group = "de.miraculixx.webserver"
version = "1.0.0"
setProperty("module_name", "webserver")

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

    implementation("dev.jorel:commandapi-shade:8.8.0")
    implementation("dev.jorel:commandapi-kotlin:8.8.0")
    implementation("de.miraculixx:minimessage-convertor:1.0.0")

    implementation("net.axay:kspigot:1.19.2")

    implementation(project(":vanilla"))
}

sourceSets {
    main {
        resources.srcDirs("$rootDir/commons/")
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
        dependsOn(reobfJar)
    }
}