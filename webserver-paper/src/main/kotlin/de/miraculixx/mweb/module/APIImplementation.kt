package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mweb.api.MWebAPI
import de.miraculixx.mweb.api.data.WhitelistFile
import de.miraculixx.mweb.api.data.WhitelistType
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import java.io.File
import java.util.*
import kotlin.time.Duration

class APIImplementation : MWebAPI(), WhitelistHandling {
    init {
        INSTANCE = this
    }

    override fun whitelistFile(path: String, access: WhitelistType, restriction: String?, duration: Duration?, maxDownloads: Int?): Pair<String, WhitelistFile>? =
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
}