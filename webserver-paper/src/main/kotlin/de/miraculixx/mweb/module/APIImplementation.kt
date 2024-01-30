package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.interfaces.LogPayloads
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mvanilla.web.WebClient
import de.miraculixx.mweb.MWeb
import de.miraculixx.mweb.api.MWebAPI
import de.miraculixx.mweb.api.data.*
import net.axay.kspigot.extensions.server
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.time.Duration

class APIImplementation : MWebAPI(), WhitelistHandling, LogPayloads {
    init {
        INSTANCE = this
    }

    override fun whitelistFile(path: String, access: WhitelistType, restriction: String?, duration: Duration?, maxDownloads: Int?): Pair<String, AccessDownload>? =
        Audience.empty().whitelistFile(path, access, restriction, duration, maxDownloads)

    override fun removeWhitelist(id: String) = Audience.empty().removeWhitelist(id)

    override fun sendFileAsResourcePack(path: String, targets: Set<UUID>, force: Boolean): Boolean {
        val rpInfo = Audience.empty().createResourcePackAccess(path) ?: return false
        taskRunLater(60 * 20) { Audience.empty().removeWhitelist(rpInfo.data) }
        targets.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid) ?: return@forEach
            player.setResourcePack(rpInfo.link, rpInfo.hash, rpInfo.prompt, force)
        }
        return true
    }

    override fun sendFilesAsResourcePacks(paths: Set<String>, targets: Set<UUID>, force: Boolean): Boolean {
        val rpInfos = paths.mapNotNull {
            val access = Audience.empty().createResourcePackAccess(it)
            if (access == null) null
            else access to UUID.randomUUID()
        }
        val rpData = rpInfos.map {
            ResourcePackInfo.resourcePackInfo(it.second, URI(it.first.link), it.first.hash.decodeToString())
        }
        val request = ResourcePackRequest.resourcePackRequest().required(force).packs(rpData).build()
        taskRunLater(60 * 20) { rpInfos.forEach { Audience.empty().removeWhitelist(it.first.data) } }
        targets.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid) ?: return@forEach
            player.sendResourcePacks(request)
        }
        return true
    }

    override fun zipFolder(folder: File, target: File): Boolean {
        return if (folder.exists() && folder.isDirectory) {
            Zipping.zipFolder(folder, target)
            true
        } else false
    }

    override fun unZipFolder(zip: File, target: File): Boolean {
        return if (zip.exists() && FileType.getType(zip.extension) == FileType.ARCHIVE) {
            Zipping.unzipArchive(zip, target)
            true
        } else false
    }

    override fun registerLogSending(modInstance: Any, modID: String, webhookURL: URL, files: Set<File>, cooldown: Int, zip: Boolean): Boolean {
        val plugin = modInstance as? JavaPlugin ?: return false
        val metadata = plugin.description
        if (metadata.name != modID) return false
        val logSender = LogPayloadData(
            "-1", -1, MWeb.INSTANCE.description.version,
            LogPayloadModData(metadata.name, metadata.version),
            LogPayloadServerData(server.minecraftVersion, server.version, System.getProperty("os.name"))
        )
        WebClient.logbacks[modID] = LogPayload(files, cooldown, webhookURL.toString(), zip, logSender)
        consoleAudience.sendMessage(prefix + cmp("Registered log sending for $modID"))
        return true
    }

    override fun unregisterLogSending(modInstance: Any, modID: String): Boolean {
        val plugin = modInstance as? JavaPlugin ?: return false
        val metadata = plugin.description
        if (metadata.name != modID) return false
        return WebClient.logbacks.remove(modID) != null
    }
}