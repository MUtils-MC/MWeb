package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.await.AwaitChatMessage
import de.miraculixx.mweb.await.AwaitConfirm
import de.miraculixx.mweb.gui.items.ItemWhitelists
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.time.Duration

class ActionWhitelists(previous: CustomInventory, download: Boolean) : GUIEvent {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event
        val meta = item.itemMeta ?: return@event
        val provider = inv.itemProvider as? ItemWhitelists ?: return@event

        if (meta.customModel != 100) {
            previous.update()
            previous.open(player)
            return@event
        }
        val id = meta.persistentDataContainer.get(provider.idKey) ?: return@event
        val data = if (download) ServerData.getFileData(id) else ServerData.getUploadData(id)
        val perm = if (download) "whitelist" else "upload"

        if (data == null) {
            player.soundError()
            inv.update()
            return@event
        }

        when (it.click) {
            ClickType.LEFT -> {
                if (!player.permVisual("mweb.$perm.toggle")) return@event
                data.disabled = player.toggle(data.disabled)
                inv.update()
            }

            ClickType.RIGHT -> {
                player.closeInventory()
                player.sendMessage(prefix + msg("command.copyLink", listOf(ServerData.getLink(id, download))))
                player.soundEnable()
            }

            ClickType.NUMBER_KEY -> {
                when (it.hotbarButton) {
                    // Delete Whitelist
                    0 -> {
                        if (!player.permVisual("mweb.$perm.delete")) return@event
                        AwaitConfirm(player, {
                            if (!player.permVisual("mweb.$perm.delete", true)) return@AwaitConfirm
                            if (download) ServerData.removeWhitelist(id) else ServerData.removeUpload(id)
                            player.soundDelete()
                            inv.update()
                            inv.open(player)
                        }) {
                            player.click()
                            inv.open(player)
                        }
                    }
                    // Rise max downloads
                    1 -> {
                        if (!player.permVisual("mweb.whitelist.edit") || data !is AccessDownload) return@event
                        data.maxAmount?.let {
                            AwaitChatMessage(false, player, "Amount", 30, null, false, cmp("\n "), {
                                if (!player.permVisual("mweb.whitelist.edit")) return@AwaitChatMessage
                                val amount = it.toIntOrNull()
                                if (amount == null) {
                                    player.soundError()
                                } else {
                                    if (data.maxAmount == null) data.maxAmount = amount
                                    else data.maxAmount = data.maxAmount!! + amount
                                    player.soundEnable()
                                }
                            }) {
                                inv.update()
                                inv.open(player)
                            }
                        } ?: player.soundStone()
                    }
                    // Rise timeout
                    2 -> {
                        if (!player.permVisual("mweb.$perm.edit")) return@event
                        data.timeout?.let {
                            AwaitChatMessage(false, player, "Time", 30, null, false, cmp("\n"), {
                                if (!player.permVisual("mweb.$perm.edit")) return@AwaitChatMessage
                                val duration = try {
                                    Duration.parse(it)
                                } catch (_: Exception) {
                                    player.soundError()
                                    return@AwaitChatMessage
                                }
                                if (data.timeout != null) data.timeout = (data.timeout!! + duration.inWholeMilliseconds)
                                player.soundEnable()
                            }) {
                                inv.update()
                                inv.open(player)
                            }
                        } ?: player.soundStone()
                    }

                    else -> player.soundStone()
                }
            }

            else -> player.soundStone()
        }
    }
}