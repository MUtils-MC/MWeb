
rootProject.name = "MinecraftWebServer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

include("webserver-fabric")
include("webserver-paper")
include("vanilla")
include("api")