package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.messages.click
import de.miraculixx.mvanilla.messages.soundEnable
import de.miraculixx.mvanilla.messages.soundStone
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemCreateWhitelist
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.items.ItemWhitelists
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.module.permVisual
import net.minecraft.server.level.ServerPlayer
import java.io.File

class ActionFilesUpload : GUIEvent, ActionFiles, WhitelistHandling {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.player as? ServerPlayer ?: return@event
        val item = it.item
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (item.getID()) {
            100 -> {
                val path = item.getTagElement("de.miraculixx.api")?.getString(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)
                if (!file.isDirectory) {
                    player.soundStone()
                    return@event
                }

                when (it.click) {
                    GUIClick.LEFT_CLICK -> file.navigate(player, provider, inv)

                    GUIClick.HOTKEY_SWAP -> {
                        when (it.clickId) {
                            //Create global link
                            0 -> {
                                if (!player.permVisual("mweb.upload.global")) return@event
                                player.whitelistUpload(path, WhitelistType.GLOBAL)
                                player.soundEnable()
                                inv.update()
                            }
                            //Create a user specific link
                            1 -> {
                                if (!player.permVisual("mweb.upload.privat")) return@event
                                player.whitelistUpload(path, WhitelistType.USER_RESTRICTED, player.uuid.toString())
                                player.soundEnable()
                                inv.update()
                            }
                            //Create a custom link
                            2 -> {
                                if (!player.permVisual("mweb.upload.custom")) return@event
                                GUITypes.CREATE_CUSTOM_WHITELIST.buildInventory(player, "${player.uuid}-CREATE_WHITELIST", ItemCreateWhitelist(path, false), ActionCreateWhitelist(false))
                            }

                            //Manage file links
                            3 -> {
                                if (!player.permVisual("mweb.upload.manage")) return@event
                                if (ServerData.getUploads(file.path).isEmpty()) {
                                    player.soundStone()
                                    return@event
                                }
                                player.click()
                                GUITypes.MANAGE_WHITELISTS.buildInventory(player, "${player.uuid}-MANAGE_WHITELIST", ItemWhitelists(file, false), ActionWhitelists(inv, false))
                            }

                            else -> player.soundStone()
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.openManager(provider)
            2 -> player.openWhitelist(provider)
            3 -> player.soundStone()
        }
    }
}