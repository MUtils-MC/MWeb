package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mweb.api.MWebAPI
import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mweb.gui.logic.item.native
import de.miraculixx.mweb.server
import net.kyori.adventure.audience.Audience
import net.silkmc.silk.core.task.mcCoroutineTask
import java.io.File
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class APIImplementation : MWebAPI(), WhitelistHandling {
    init {
        INSTANCE = this
    }

    override fun whitelistFile(path: String, access: WhitelistType, restriction: String?, duration: Duration?, maxDownloads: Int?): Pair<String, AccessDownload>? =
        Audience.empty().whitelistFile(path, access, restriction, duration, maxDownloads)

    override fun removeWhitelist(id: String) = Audience.empty().removeWhitelist(id)

    override fun sendFileAsResourcePack(path: String, targets: Set<UUID>, force: Boolean): Boolean {
        val rpInfo = Audience.empty().createResourcePackAccess(path) ?: return false
        mcCoroutineTask(true, delay = 1.minutes) { Audience.empty().removeWhitelist(rpInfo.data) }
        targets.forEach { uuid ->
            val player = server.playerList.getPlayer(uuid) ?: return@forEach
            player.sendTexturePack(rpInfo.link, rpInfo.hash.decodeToString(), force, rpInfo.prompt.native())
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