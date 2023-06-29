package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.await.AwaitChatMessage
import de.miraculixx.mweb.gui.items.ItemCreateWhitelist
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import net.axay.kspigot.items.customModel
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.net.URLEncoder
import kotlin.time.Duration

class ActionCreateWhitelist(download: Boolean) : GUIEvent, WhitelistHandling {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event
        val meta = item.itemMeta
        val provider = inv.itemProvider as ItemCreateWhitelist

        when (meta.customModel) {
            1 -> {
                provider.restriction = null
                provider.whitelistType = when (provider.whitelistType) {
                    WhitelistType.GLOBAL -> WhitelistType.USER_RESTRICTED
                    WhitelistType.USER_RESTRICTED -> WhitelistType.PASSPHRASE_RESTRICTED
                    WhitelistType.PASSPHRASE_RESTRICTED -> WhitelistType.GLOBAL
                }
                player.click()
                inv.update()
            }

            2 -> {
                AwaitChatMessage(false, player, "Timeout", 30, null, false, msg("event.setTimeout"), {
                    provider.timeout = try {
                        Duration.parse(it)
                    } catch (_: Exception) {
                        player.soundError()
                        return@AwaitChatMessage
                    }
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            3 -> {
                AwaitChatMessage(false, player, "Max Amount", 30, null, false, msg("event.setMaxAmount"), {
                    val amount = it.toIntOrNull()
                    if (amount == null || amount <= 0) {
                        player.soundError()
                        return@AwaitChatMessage
                    }
                    provider.maxRequests = amount
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            4 -> {
                AwaitChatMessage(false, player, "Player", 60, null, false, msg("event.setPlayer"), {
                    val target = Bukkit.getOfflinePlayer(it)
                    if (!target.hasPlayedBefore() || !ServerData.checkPlayer(target.uniqueId)) {
                        player.sendMessage(prefix + cmp(msgString("event.noPlayer", listOf(it))))
                        player.soundError()
                        return@AwaitChatMessage
                    }
                    provider.restriction = target.uniqueId.toString()
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            5 -> {
                AwaitChatMessage(false, player, "Passphrase", 60, null, false, msg("event.setPassphrase"), {
                    val encoded = URLEncoder.encode(it, Charsets.UTF_8)
                    provider.restriction = encoded
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            6 -> {
                AwaitChatMessage(false, player, "Max File Size (mb)", 30, null, false, msg("event.setMaxFileSize"), {
                    provider.maxFileSize = it.toLongOrNull()
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            7 -> {
                AwaitChatMessage(false, player, "Max File Amount", 30, null, false, msg("event.setMaxFileSize"), {
                    provider.maxFileAmount = it.toIntOrNull() ?: 0
                    player.soundEnable()
                }) {
                    inv.update()
                    inv.open(player)
                }
            }

            10 -> {
                if (!player.permVisual("mweb.${if (download) "whitelist" else "upload"}.custom", true)) return@event
                val type = provider.whitelistType
                if ((type == WhitelistType.PASSPHRASE_RESTRICTED || type == WhitelistType.USER_RESTRICTED) && provider.restriction == null) {
                    player.soundError()
                    return@event
                }

                player.closeInventory()
                if (download) player.whitelistFile(provider.path, type, provider.restriction, provider.timeout, provider.maxRequests)
                else player.whitelistUpload(provider.path, type, provider.restriction, provider.maxFileSize, provider.maxFileAmount, provider.timeout)
            }
        }
    }
}