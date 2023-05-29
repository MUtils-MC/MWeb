package de.miraculixx.webserver

import de.miraculixx.webserver.commands.MainCommand
import de.miraculixx.webserver.utils.*
import de.miraculixx.webserver.utils.messages.Localization
import de.miraculixx.webserver.utils.messages.cError
import de.miraculixx.webserver.utils.messages.cmp
import de.miraculixx.webserver.utils.messages.plus
import de.miraculixx.webserver.web.WebServer
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
class MWebServer: KSpigot() {
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
    }

    override fun startup() {
        WebServer.startServer()
        ServerData.loadData()

        // Register commands
        MainCommand()
    }

    override fun shutdown() {
        WebServer.stopServer()
        ServerData.saveData()
    }
}

val PluginInstance by lazy { MWebServer.INSTANCE }