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
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")


    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")
    implementation("dev.jorel:commandapi-bukkit-kotlin:9.0.3")

    implementation("net.axay:kspigot:1.20.1")

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