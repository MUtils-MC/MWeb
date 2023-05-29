package de.miraculixx.webserver

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.Localization
import de.miraculixx.mvanilla.messages.cError
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.webserver.commands.MainCommand
import de.miraculixx.webserver.module.GlobalListener
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIConfig
import kotlinx.serialization.decodeFromString
import net.axay.kspigot.extensions.console
import net.axay.kspigot.main.KSpigot
import java.io.File

/**
 * Feature Roadmap
 * - Zip Files
 * - Whitelist files (publish)
 *      -> global
 *      -> player only
 *      -> IP only
 *    -> Unlimited
 *    -> Time limited
 *    -> Request limited
 * - Send Texture Packs
 *      -> Auto Packing
 * - Clear temp folder
 */
class MWebServer : KSpigot() {
    companion object {
        lateinit var INSTANCE: KSpigot
        lateinit var localization: Localization
    }

    override fun load() {
        INSTANCE = this
        consoleAudience = console
        configFolder = dataFolder
        if (!configFolder.exists()) configFolder.mkdir()

        try {
            settings = WebServer.json.decodeFromString(File(configFolder, "settings").readText().ifBlank { "{}" })
        } catch (e: Exception) {
            settings = Settings()
            consoleAudience.sendMessage(prefix + cmp("Failed to read settings! Reason: ", cError))
            consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown"))
        }

        val languages = listOf("en_US", "de_DE").map { it to javaClass.getResourceAsStream("/language/$it.yml") }
        localization = Localization(File("${configFolder.path}/language"), settings.lang, languages)

        CommandAPI.onLoad(CommandAPIConfig().verboseOutput(false).silentLogs(true))
    }

    override fun startup() {
        WebServer.startServer()
        ServerData.loadData()

        // Register listener
        CommandAPI.onEnable(this)
        MainCommand()
        GlobalListener
    }

    override fun shutdown() {
        WebServer.stopServer()
        ServerData.saveData()
        CommandAPI.onDisable()
    }
}

val PluginInstance by lazy { MWebServer.INSTANCE }