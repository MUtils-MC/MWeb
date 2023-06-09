package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.await.AwaitChatMessage
import de.miraculixx.mweb.await.AwaitConfirm
import de.miraculixx.mweb.gui.items.ItemWhitelists
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.module.permVisual
import net.minecraft.server.level.ServerPlayer
import kotlin.time.Duration

class ActionWhitelists(previous: CustomInventory) : GUIEvent {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.player as? ServerPlayer ?: return@event
        val item = it.item
        val provider = inv.itemProvider as? ItemWhitelists ?: return@event

        if (item.getID() != 100) {
            previous.update()
            previous.open(player)
            return@event
        }
        val id = item.getTagElement("de.miraculixx.api")?.getString(provider.idKey) ?: return@event
        val data = ServerData.getFileData(id)

        if (data == null) {
            player.soundError()
            inv.update()
            return@event
        }

        when (it.click) {
            GUIClick.LEFT_CLICK -> {
                if (!player.permVisual("mweb.whitelist.toggle")) return@event
                data.disabled = player.toggle(data.disabled)
                inv.update()
            }

            GUIClick.RIGHT_CLICK -> {
                player.closeContainer()
                player.sendMessage(prefix + msg("command.copyLink", listOf(ServerData.getLink(id))))
                player.soundEnable()
            }

            GUIClick.HOTKEY_SWAP -> {
                when (it.clickId) {
                    // Delete Whitelist
                    0 -> {
                        if (!player.permVisual("mweb.whitelist.delete")) return@event
                        AwaitConfirm(player, {
                            if (!player.permVisual("mweb.whitelist.delete", true)) return@AwaitConfirm
                            ServerData.removeWhitelist(id)
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
                        if (!player.permVisual("mweb.whitelist.edit")) return@event
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
                        if (!player.permVisual("mweb.whitelist.edit")) return@event
                        data.timeout?.let {
                            AwaitChatMessage(false, player, "Time", 30, null, false, cmp("\n"), {
                                if (!player.permVisual("mweb.whitelist.edit")) return@AwaitChatMessage
                                val duration = try {
                                    Duration.parse(it)
                                } catch (_: Exception) {
                                    player.soundError()
                                    return@AwaitChatMessage
                                }
                                if (data.timeout != null) data.timeout = data.timeout!! + duration.inWholeMilliseconds
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