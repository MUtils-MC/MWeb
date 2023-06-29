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
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesUpload : GUIEvent, ActionFiles, WhitelistHandling {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event
        val meta = item.itemMeta ?: return@event
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (meta.customModel) {
            100 -> {
                val path = meta.persistentDataContainer.get(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)
                if (!file.isDirectory) {
                    player.soundStone()
                    return@event
                }

                when (it.click) {
                    ClickType.LEFT -> file.navigate(player, provider, inv)

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
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
                                player.whitelistUpload(path, WhitelistType.USER_RESTRICTED, player.uniqueId.toString())
                                player.soundEnable()
                                inv.update()
                            }
                            //Create a custom link
                            2 -> {
                                if (!player.permVisual("mweb.upload.custom")) return@event
                                GUITypes.CREATE_CUSTOM_WHITELIST.buildInventory(player, "${player.uniqueId}-CREATE_WHITELIST", ItemCreateWhitelist(path, false), ActionCreateWhitelist(false))
                            }

                            //Manage file links
                            3 -> {
                                if (!player.permVisual("mweb.upload.manage")) return@event
                                if (ServerData.getUploads(file.path).isEmpty()) {
                                    player.soundStone()
                                    return@event
                                }
                                player.click()
                                GUITypes.MANAGE_WHITELISTS.buildInventory(player, "${player.uniqueId}-MANAGE_WHITELIST", ItemWhitelists(file, false), ActionWhitelists(inv, false))
                            }
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