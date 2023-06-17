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
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.axay.kspigot.extensions.console
import net.axay.kspigot.extensions.pluginManager
import net.axay.kspigot.main.KSpigot
import net.axay.kspigot.runnables.taskRunLater
import java.io.File

class MWeb : KSpigot() {
    companion object {
        lateinit var INSTANCE: KSpigot
        lateinit var localization: Localization
    }

    override fun load() {
        INSTANCE = this
        consoleAudience = console
        configFolder = File(dataFolder.parent, "MUtils/Web")
        if (!configFolder.exists()) configFolder.mkdirs()

        try {
            settings = WebServer.json.decodeFromString(File(configFolder, "settings.json").takeIf { it.isFile }?.readText()?.ifBlank { "{}" } ?: "{}")
        } catch (e: Exception) {
            settings = Settings()
            consoleAudience.sendMessage(prefix + cmp("Failed to read settings! Reason: ", cError))
            consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown"))
        }

        val languages = listOf("en_US", "de_DE").map { it to javaClass.getResourceAsStream("/language/$it.yml") }
        localization = Localization(File("${configFolder.path}/language"), settings.lang, languages)

        val responseFolder = File(configFolder, "responses")
        if (!responseFolder.exists()) responseFolder.mkdir()
        File(responseFolder, "download.html").takeIf { !it.exists() }?.let { dumpRessourceFile(it, "/responses/download.html") }
        File(responseFolder, "forbidden.html").takeIf { !it.exists() }?.let { dumpRessourceFile(it, "/responses/forbidden.html") }
        File(responseFolder, "invalid.html").takeIf { !it.exists() }?.let { dumpRessourceFile(it, "/responses/invalid.html") }
        File(responseFolder, "notfound.html").takeIf { !it.exists() }?.let { dumpRessourceFile(it, "/responses/notfound.html") }
        File(responseFolder, "index.html").takeIf { !it.exists() }?.let { dumpRessourceFile(it, "/responses/index.html") }


        @Suppress("DEPRECATION") // Papers new description is incompatible with old versions
        if (!WebServer.checkVersion(description.version.toIntOrNull() ?: 0)) {
            pluginManager.disablePlugin(this)
            return
        }

        APIImplementation()
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).verboseOutput(false).silentLogs(true))
    }

    override fun startup() {
        LoaderImplementation()
        ServerData.loadData()

        // Start after first tick
        taskRunLater(1) { WebServer.startServer() }

        // Register listener
        CommandAPI.onEnable()
        MainCommand()
        GlobalListener
    }

    override fun shutdown() {
        println(WebServer.isStarted)
        if (!WebServer.isStarted) return
        WebServer.stopServer()
        ServerData.saveData()
        CommandAPI.onDisable()
        File(configFolder, "settings.json").writeText(WebServer.jsonFull.encodeToString(settings))
    }

    private fun dumpRessourceFile(file: File, location: String) {
        javaClass.getResourceAsStream(location)?.let { file.writeBytes(it.readAllBytes()) } ?: println("Failed to load $location!")
    }
}
