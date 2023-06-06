package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.data.Head64
import de.miraculixx.mvanilla.data.WhitelistType
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.toUUID
import de.miraculixx.mweb.gui.logic.InventoryUtils
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import kotlin.time.Duration

class ItemCreateWhitelist(val path: String) : ItemProvider {
    private val loreInfo = cmp("• ", NamedTextColor.DARK_GRAY, bold = true) + cmp("Settings", cHighlight, underlined = true)
    private val msgDot = cmp("  • ", NamedTextColor.DARK_GRAY)
    private val msgTimeout = msgString("items.timeout.n")
    private val msgMaxDownloads = msgString("items.maxDownloads.n")
    private val msgPassphrase = msgString("items.passphrase.n")
    private val msgUser = msgString("items.userRestriction.n")
    private val msgConfirm = cmp(msgString("common.confirm"), NamedTextColor.GREEN, true)

    var whitelistType = WhitelistType.GLOBAL
    var timeout: Duration? = null
    var maxRequests: Int? = null
    var restriction: String? = null

    override fun getSlotMap(): Map<Int, ItemStack> {
        return buildMap {
            put(11, itemStack(Material.PLAYER_HEAD) {
                meta {
                    name = cmp(msgString("items.${whitelistType.name}.n"), cHighlight, true)
                    lore(msgList("items.${whitelistType.name}.l"))
                    customModel = 1
                }
                itemMeta = (itemMeta as SkullMeta).skullTexture(whitelistType.head.value)
            })

            put(13, itemStack(Material.CLOCK) {
                meta {
                    name = cmp(msgTimeout, cHighlight, true)
                    lore(msgList("items.timeout.l") + getLoreSettings())
                    customModel = 2
                }
            })
            put(14, itemStack(Material.HOPPER) {
                meta {4
                    name = cmp(msgMaxDownloads, cHighlight, true)
                    lore(msgList("items.maxDownloads.l") + getLoreSettings())
                    customModel = 3
                }
            })

            when (whitelistType) {
                WhitelistType.GLOBAL -> put(15, InventoryUtils.phPrimary)
                WhitelistType.USER_RESTRICTED -> {
                    put(15, itemStack(Material.PLAYER_HEAD) {
                        meta<SkullMeta> {
                            val uuid = restriction?.toUUID()
                            uuid?.let { owningPlayer = Bukkit.getOfflinePlayer(it) }
                            name = cmp(msgUser, cHighlight, true)
                            lore(msgList("items.userRestriction.l") + getLoreSettings())
                            customModel = 4
                        }
                    })
                }

                WhitelistType.PASSPHRASE_RESTRICTED -> {
                    put(15, itemStack(Material.PAPER) {
                        meta {
                            name = cmp(msgPassphrase, cHighlight, true)
                            lore(msgList("items.passphrase.l") + getLoreSettings())
                            customModel = 5
                        }
                    })
                }
            }

            put(22, itemStack(Material.PLAYER_HEAD) {
                meta {
                    name = msgConfirm
                    customModel = 10
                }
                itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.CHECKMARK_GREEN.value)
            })
        }
    }

    private fun getLoreSettings(): List<Component> {
        return buildList {
            add(emptyComponent())
            add(loreInfo)
            add(msgDot + cmp("$msgTimeout: ") + cmp(timeout?.toString() ?: msgNone, cMark))
            add(msgDot + cmp("$msgMaxDownloads: ") + cmp(maxRequests?.toString() ?: msgNone, cMark))
            if (whitelistType == WhitelistType.PASSPHRASE_RESTRICTED) add(msgDot + cmp("$msgPassphrase: ") + cmp(restriction ?: msgNone, cMark))
            if (whitelistType == WhitelistType.USER_RESTRICTED) add(msgDot + cmp("$msgUser: ") + cmp(restriction ?: msgNone, cMark))
        }
    }
}