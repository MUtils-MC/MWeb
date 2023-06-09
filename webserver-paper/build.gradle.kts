plugins {
    `core-script`
    `shadow-script`
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

group = "de.miraculixx.mweb"
version = "1.1.0"
setProperty("module_name", "mweb")

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

    implementation("dev.jorel:commandapi-shade:8.8.0")
    implementation("dev.jorel:commandapi-kotlin:8.8.0")

    implementation("net.axay:kspigot:1.19.2")

    implementation(project(":vanilla"))
    implementation(project(":api"))
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