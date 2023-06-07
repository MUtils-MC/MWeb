package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.messages.click
import de.miraculixx.mvanilla.messages.soundError
import de.miraculixx.mvanilla.messages.soundStone
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import org.bukkit.entity.Player
import kotlin.io.path.Path

interface ActionFiles {
    fun File.navBack(provider: ItemFilesManage, player: Player, inv: CustomInventory) {
        val parentFile = parentFile
        if (parentFile != null) {
            provider.currentFolder = parentFile
            player.click()
            inv.update()
        } else player.soundStone()
    }

    fun File.navigate(player: Player, provider: ItemFilesManage, inv: CustomInventory) {
        if (!isDirectory) {
            player.soundStone()
            return
        }
        player.click()
        provider.currentFolder = this
        inv.update()
    }

    fun Player.openManager(provider: ItemFilesManage) {
        if (!permVisual("mweb.manage.list")) return
        click()
        GUITypes.FILE_MANAGE.buildInventory(this, "${uniqueId}-MANAGE", ItemFilesManage(provider.currentFolder, GUITypes.FILE_MANAGE), ActionFilesManage())
    }

    fun Player.openWhitelist(provider: ItemFilesManage) {
        if (!permVisual("mweb.whitelist.list")) return
        click()
        GUITypes.FILE_WHITELISTING.buildInventory(this, "${uniqueId}-WHITELIST", ItemFilesManage(provider.currentFolder, GUITypes.FILE_WHITELISTING), ActionFilesWhitelist())
    }

    fun Player.openUpload(provider: ItemFilesManage) {
        if (!permVisual("mweb.upload.list")) return
        soundError()
//        GUITypes.FILE_UPLOADING.buildInventory(this, "${uniqueId}-UPLOAD", ItemFilesManage(provider.currentFolder, GUITypes.FILE_UPLOADING), ActionFilesUpload())
    }
}