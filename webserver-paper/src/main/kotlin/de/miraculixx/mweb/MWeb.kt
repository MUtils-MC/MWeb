package de.miraculixx.mweb

import de.miraculixx.mmconvertor.MiniMessageConvertor
import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.Localization
import de.miraculixx.mvanilla.messages.cError
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.commands.MainCommand
import de.miraculixx.mweb.module.GlobalListener
import de.miraculixx.mweb.module.LoaderImplementation
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.axay.kspigot.extensions.console
import net.axay.kspigot.main.KSpigot
import net.axay.kspigot.runnables.taskRunLater
import java.io.File

/**
 * Feature Roadmap
 * - Zip Files
 * - Whitelist files (publish)
 *      -> global
 *      -> player only
 *      -> IP only (nah)
 *      -> Password protected
 *    -> Unlimited
 *    -> Time limited
 *    -> Request limited
 * - Send Texture Packs
 *      -> Auto Packing
 * - Clear temp folder
 */
class MWeb : KSpigot() {
    companion object {
        lateinit var INSTANCE: KSpigot
        lateinit var localization: Localization
    }

    override fun load() {
        MiniMessageConvertor
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

        val responseFolder = File(configFolder, "webserver-data/responses")
        if (!responseFolder.exists()) {
            responseFolder.mkdir()
            dumpRessourceFile("/responses/forbidden.html", File(responseFolder, "forbidden.html"))
            dumpRessourceFile("/responses/invalid.html", File(responseFolder, "invalid.html"))
            dumpRessourceFile("/responses/notfound.html", File(responseFolder, "notfound.html"))
            dumpRessourceFile("/responses/index.html", File(responseFolder, "index.html"))
        }

        CommandAPI.onLoad(CommandAPIConfig().verboseOutput(false).silentLogs(true))
    }

    override fun startup() {
        LoaderImplementation()
        ServerData.loadData()

        // Start after server loading
        taskRunLater(1) { WebServer.startServer() }

        // Register listener
        CommandAPI.onEnable(this)
        MainCommand()
        GlobalListener
    }

    override fun shutdown() {
        WebServer.stopServer()
        ServerData.saveData()
        CommandAPI.onDisable()
        File(configFolder, "settings.json").writeText(WebServer.jsonFull.encodeToString(settings))
    }

    private fun dumpRessourceFile(location: String, target: File) {
        javaClass.getResourceAsStream(location)?.let { target.writeBytes(it.readAllBytes()) }
    }
}

val PluginInstance by lazy { MWeb.INSTANCE }