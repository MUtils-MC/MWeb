package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.messages.click
import de.miraculixx.mvanilla.messages.soundStone
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesManage: GUIEvent {
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

                when (it.click) {
                    //Navigate into file (folder only)
                    ClickType.LEFT -> {
                        if (!file.isDirectory) {
                            player.soundStone()
                            return@event
                        }
                        player.click()
                        provider.currentFolder = file
                        inv.update()
                    }

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
                            0 -> {
                                player.closeInventory()
                                Await
                            }
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> {
                val parentFile = provider.currentFolder.parentFile
                if (parentFile != null) {
                    provider.currentFolder = parentFile
                    player.click()
                    inv.update()
                } else player.soundStone()
            }
        }
    }
}