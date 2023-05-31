package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.await.AwaitChatMessage
import de.miraculixx.mweb.await.AwaitConfirm
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesManage : GUIEvent, ActionFiles {
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
                    ClickType.LEFT -> file.navigate(player, provider, inv)

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
                            0 -> {
                                player.click()
                                AwaitChatMessage(false, player, "Rename File", 60, file.name, true, msg("event.rename"), { msg ->
                                    val parent = file.parentFile
                                    try {
                                        if (parent == null) file.renameTo(File(msg))
                                        else file.renameTo(File(file.parentFile, msg))
                                    } catch (e: Exception) {
                                        consoleAudience.sendMessage(prefix + cmp("Failed to rename file ${file.path}! Reason...", cError))
                                        consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown", cError))
                                        player.soundError()
                                        player.sendMessage(prefix + cmp(msgString("event.invalidName", listOf(msg))))
                                        return@AwaitChatMessage
                                    }
                                    inv.update()
                                    player.soundEnable()
                                }) {
                                    inv.open(player)
                                }
                            }

                            1 -> {
                                //ZIP Logic
                            }

                            2 -> {
                                player.click()
                                AwaitConfirm(player, {
                                    try {
                                        if (file.isDirectory) file.deleteRecursively()
                                        else file.delete()
                                    } catch (e: Exception) {
                                        consoleAudience.sendMessage(prefix + cmp("Failed to delete file ${file.path}! Reason...", cError))
                                        consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown", cError))
                                        player.sendMessage(prefix + cmp(msgString("event.invalidDelete", listOf(file.name))))
                                        player.closeInventory()
                                        player.soundError()
                                        return@AwaitConfirm
                                    }
                                    player.soundDelete()
                                    inv.update()
                                    inv.open(player)
                                }) {
                                    player.click()
                                    inv.open(player)
                                }
                            }

                            else -> player.soundStone()
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.soundStone()
            2 -> player.openWhitelist(provider)
            3 -> player.openDownload(provider)
        }
    }
}