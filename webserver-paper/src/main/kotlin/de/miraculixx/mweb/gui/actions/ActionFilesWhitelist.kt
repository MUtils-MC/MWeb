package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.commands.WhitelistHandling
import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.WhitelistFile
import de.miraculixx.mvanilla.data.WhitelistType
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesWhitelist: GUIEvent, WhitelistHandling, ActionFiles {
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
                if (!file.exists()) {
                    player.soundError()
                    inv.update() //Refresh files
                    player.sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(file.path)), cError))
                }

                when (it.click) {
                    //Navigate into file (folder only)
                    ClickType.LEFT -> file.navigate(player, provider, inv)

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
                            //Create global link
                            0 -> {
                                player.whitelistFile(path, WhitelistType.GLOBAL)
                                player.soundEnable()
                                inv.update()
                            }
                            //Create private link for the user
                            1 -> {
                                player.whitelistFile(path, WhitelistType.USER_RESTRICTED, player.uniqueId.toString())
                                player.soundEnable()
                                inv.update()
                            }
                            //Open custom link creator
                            2 -> {

                            }
                            //Manage file links
                            3 -> {

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
            3 -> player.openManager(provider)
        }
    }
}