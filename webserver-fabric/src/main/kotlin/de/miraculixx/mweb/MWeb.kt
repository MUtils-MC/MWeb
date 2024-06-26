package de.miraculixx.mweb

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.Localization
import de.miraculixx.mvanilla.messages.cError
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.commands.MainCommand
import de.miraculixx.mweb.module.APIImplementation
import de.miraculixx.mweb.module.GlobalListener
import de.miraculixx.mweb.module.LoaderImplementation
import kotlinx.serialization.encodeToString
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.server.MinecraftServer
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Server
import net.silkmc.silk.core.task.mcCoroutineTask
import java.io.File

lateinit var localization: Localization
lateinit var server: MinecraftServer
lateinit var adventure: FabricServerAudiences

fun init() {
    MainCommand()
    var isLoaded = false
    Events.Server.postStart.listen event@{ event ->
        server = event.server
        adventure = FabricServerAudiences.of(server)
        consoleAudience = adventure.console()

        configFolder = File("${server.serverDirectory.path}/config", "MUtils/Web")
        if (!configFolder.exists()) configFolder.mkdirs()

        try {
            settings = WebServer.json.decodeFromString(File(configFolder, "settings.json").takeIf { it.isFile }?.readText()?.ifBlank { "{}" } ?: "{}")
        } catch (e: Exception) {
            settings = Settings()
            consoleAudience.sendMessage(prefix + cmp("Failed to read settings! Reason: ", cError))
            consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown"))
        }

        val languages = listOf("en_US", "de_DE").map { it to Unit::class.java.getResourceAsStream("/language/$it.yml") }
        localization = Localization(File("${configFolder.path}/language"), settings.lang, languages)

        val responseFolder = File(configFolder, "responses")
        if (!responseFolder.exists()) responseFolder.mkdir()
        File(responseFolder, "download.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/download.html")
        File(responseFolder, "forbidden.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/forbidden.html")
        File(responseFolder, "invalid.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/invalid.html")
        File(responseFolder, "notfound.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/notfound.html")
        File(responseFolder, "index.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/index.html")
        File(responseFolder, "upload.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/upload.html")
        File(responseFolder, "uploaded.html").takeIf { !it.exists() }?.dumpRessourceFile("/responses/uploaded.html")


        val container = FabricLoader.getInstance().getModContainer("mutils-web").get()
        if (!WebServer.checkVersion(container.metadata.version.friendlyString.toIntOrNull() ?: 0)) {
            return@event
        }

        ServerData.loadData()

        // Start after first tick
        mcCoroutineTask { WebServer.startServer() }

        APIImplementation()
        LoaderImplementation()
        GlobalListener
        isLoaded = true
    }

    Events.Server.preStop.listen {
        WebServer.stopServer()
        ServerData.saveData()
        File(configFolder, "settings.json").writeText(WebServer.jsonFull.encodeToString(settings))
    }
}

private fun File.dumpRessourceFile(location: String) {
    Unit::class.java.getResourceAsStream(location)?.let { writeBytes(it.readAllBytes()) }
}
