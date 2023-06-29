package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemCreateWhitelist
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.items.ItemLoading
import de.miraculixx.mweb.gui.items.ItemWhitelists
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.module.permVisual
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer
import java.io.File

class ActionFilesWhitelist : GUIEvent, WhitelistHandling, ActionFiles {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.player as? ServerPlayer ?: return@event
        val item = it.item
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (item.getID()) {
            100 -> {
                val path = item.getTagElement("de.miraculixx.api")?.getString(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)
                if (!file.exists()) {
                    player.soundError()
                    inv.update() //Refresh files
                    player.sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(file.path)), cError))
                }

                when (it.click) {
                    //Navigate into file (folder only)
                    GUIClick.LEFT_CLICK -> file.navigate(player, provider, inv)

                    GUIClick.HOTKEY_SWAP -> {
                        when (it.clickId) {
                            //Create global link
                            0 -> {
                                if (!player.permVisual("mweb.whitelist.global")) return@event
                                player.whitelistFile(path, WhitelistType.GLOBAL)
                                player.soundEnable()
                                inv.update()
                            }
                            //Create a private link for the user
                            1 -> {
                                if (!player.permVisual("mweb.whitelist.privat")) return@event
                                if (file.isDirectory) {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        GUITypes.LOADING.buildInventory(player, "LOADING", ItemLoading(), ActionEmpty())
                                        player.whitelistFile(path, WhitelistType.USER_RESTRICTED, player.uuid.toString())
                                        inv.update()
                                        inv.open(player)
                                    }
                                } else {
                                    player.whitelistFile(path, WhitelistType.USER_RESTRICTED, player.uuid.toString())
                                    inv.update()
                                }
                                player.soundEnable()
                            }
                            //Open custom link creator
                            2 -> {
                                if (!player.permVisual("mweb.whitelist.custom")) return@event
                                GUITypes.CREATE_CUSTOM_WHITELIST.buildInventory(player, "${player.uuid}-CREATE_WHITELIST", ItemCreateWhitelist(path, true), ActionCreateWhitelist(true))
                            }
                            //Manage file links
                            3 -> {
                                if (!player.permVisual("mweb.whitelist.manage")) return@event
                                if (ServerData.getWhitelists(file.path).isEmpty()) {
                                    player.soundStone()
                                    return@event
                                }
                                player.click()
                                GUITypes.MANAGE_WHITELISTS.buildInventory(player, "${player.uuid}-MANAGE_WHITELIST", ItemWhitelists(file, true), ActionWhitelists(inv, true))
                            }

                            else -> player.soundStone()
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.openManager(provider)
            2 -> player.soundStone()
            3 -> player.openUpload(provider)
        }
    }
}