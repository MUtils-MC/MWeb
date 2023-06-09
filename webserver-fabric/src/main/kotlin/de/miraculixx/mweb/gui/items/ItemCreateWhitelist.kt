package de.miraculixx.mweb.gui.items

import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.toUUID
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.item.InventoryUtils
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.setID
import de.miraculixx.mweb.gui.logic.item.setLore
import de.miraculixx.mweb.gui.logic.item.setName
import de.miraculixx.mweb.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setSkullPlayer
import net.silkmc.silk.core.item.setSkullTexture
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
            put(11, itemStack(Items.PLAYER_HEAD) {
                setName(cmp(msgString("items.${whitelistType.name}.n"), cHighlight, true))
                setLore(msgList("items.${whitelistType.name}.l"))
                setID(1)
                setSkullTexture(whitelistType.head.value)
            })

            put(13, itemStack(Items.CLOCK) {
                setName(cmp(msgTimeout, cHighlight, true))
                setLore(msgList("items.timeout.l") + getLoreSettings())
                setID(2)
            })
            put(14, itemStack(Items.HOPPER) {
                setName(cmp(msgMaxDownloads, cHighlight, true))
                setLore(msgList("items.maxDownloads.l") + getLoreSettings())
                setID(3)
            })

            when (whitelistType) {
                WhitelistType.GLOBAL -> put(15, InventoryUtils.phPrimary)
                WhitelistType.USER_RESTRICTED -> {
                    put(15, itemStack(Items.PLAYER_HEAD) {
                        val uuid = restriction?.toUUID()
                        uuid?.let { server.playerList.getPlayer(it)?.let { it1 -> setSkullPlayer(it1) } }
                        setName(cmp(msgUser, cHighlight, true))
                        setLore(msgList("items.userRestriction.l") + getLoreSettings())
                        setID(4)
                    })
                }

                WhitelistType.PASSPHRASE_RESTRICTED -> {
                    put(15, itemStack(Items.PAPER) {
                        setName(cmp(msgPassphrase, cHighlight, true))
                        setLore(msgList("items.passphrase.l") + getLoreSettings())
                        setID(5)
                    })
                }
            }

            put(22, itemStack(Items.PLAYER_HEAD) {
                setName(msgConfirm)
                setID(10)
                setSkullTexture(Head64.CHECKMARK_GREEN.value)
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