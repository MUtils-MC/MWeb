package de.miraculixx.mweb.gui.items

import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mweb.api.data.WhitelistType
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

class ItemCreateWhitelist(val path: String, private val download: Boolean) : ItemProvider {
    private val loreInfo = cmp("• ", NamedTextColor.DARK_GRAY, bold = true) + cmp("Settings", cHighlight, underlined = true)
    private val msgDot = cmp("  • ", NamedTextColor.DARK_GRAY)
    private val msgTimeout = msgString("items.timeout.n")
    private val msgMaxDownloads = msgString("items.maxDownloads.n")
    private val msgPassphrase = msgString("items.passphrase.n")
    private val msgUser = msgString("items.userRestriction.n")
    private val msgMaxFileSize = msgString("items.maxFileSize.n")
    private val msgMaxFileAmount = msgString("items.maxFileAmount.n")
    private val msgConfirm = cmp(msgString("common.confirm"), NamedTextColor.GREEN, true)

    var whitelistType = WhitelistType.GLOBAL
    var timeout: Duration? = null
    var maxRequests: Int? = null
    var restriction: String? = null
    var maxFileSize: Long? = null
    var maxFileAmount: Int = 1

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

            if (download) {
                put(14, itemStack(Material.HOPPER) {
                    meta {
                        name = cmp(msgMaxDownloads, cHighlight, true)
                        lore(msgList("items.maxDownloads.l") + getLoreSettings())
                        customModel = 3
                    }
                })
            } else {
                put(14, itemStack(Material.COMPARATOR) {
                    meta {
                        name = cmp(msgMaxFileSize, cHighlight, true)
                        lore(msgList("items.maxFileSize.l") + getLoreSettings())
                        customModel = 6
                    }
                })
                put(15, itemStack(Material.CHEST) {
                    meta {
                        name = cmp(msgMaxFileAmount, cHighlight, true)
                        lore(msgList("items.maxFileAmount.l") + getLoreSettings())
                        customModel = 7
                    }
                })
            }


            val restrictionSlot = if (download) 15 else 16
            when (whitelistType) {
                WhitelistType.GLOBAL -> put(restrictionSlot, InventoryUtils.phPrimary)
                WhitelistType.USER_RESTRICTED -> {
                    put(restrictionSlot, itemStack(Material.PLAYER_HEAD) {
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
                    put(restrictionSlot, itemStack(Material.PAPER) {
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
            if (download) add(msgDot + cmp("$msgMaxDownloads: ") + cmp(maxRequests?.toString() ?: msgNone, cMark))
            else {
                add(msgDot + cmp("$msgMaxFileSize: ") + cmp(maxFileSize?.toString() ?: msgNone, cMark))
                add(msgDot + cmp("$msgMaxFileAmount: ") + cmp(maxFileAmount.toString(), cMark))
            }
            if (whitelistType == WhitelistType.PASSPHRASE_RESTRICTED) add(msgDot + cmp("$msgPassphrase: ") + cmp(restriction ?: msgNone, cMark))
            if (whitelistType == WhitelistType.USER_RESTRICTED) add(msgDot + cmp("$msgUser: ") + cmp(restriction ?: msgNone, cMark))
        }
    }
}