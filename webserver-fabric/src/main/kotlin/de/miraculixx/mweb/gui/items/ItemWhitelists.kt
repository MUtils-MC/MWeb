package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.setID
import de.miraculixx.mweb.gui.logic.item.setLore
import de.miraculixx.mweb.gui.logic.item.setName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setSkullTexture
import net.silkmc.silk.nbt.set
import java.io.File

class ItemWhitelists(private val file: File?) : ItemProvider {
    private val msgDot = cmp("  • ", NamedTextColor.DARK_GRAY)
    private val msgButton = cmp(msgString("common.button") + " ", cHighlight)
    private val msgRiseMaxDownloads = msgString("event.action.riseDownloads")
    private val msgRiseTimeout = msgString("event.action.riseTimeout")
    private val msgCopyLink = msgString("event.action.copyLink")

    val idKey = "file-whitelist-id"

    override fun getBooleanMap(from: Int, to: Int): Map<ItemStack, Boolean> {
        return buildMap {
            val whitelists = file?.let { f -> ServerData.getWhitelists(f.path) } ?: ServerData.getWhitelists()
            whitelists.forEach { (id, data) ->
                val file = File(data.path)
                put(itemStack(Items.PLAYER_HEAD) {
                    setName(cmp(file.name, cHighlight))
                    setLore(
                        listOf(cmp(file.path, NamedTextColor.DARK_GRAY)) +
                                data.fullLore(msgDot) +
                                buildList {
                                    add(msgDot + cmp(data.disabled.not().msg()))
                                    add(emptyComponent())
                                    add(msgClickLeft + cmp("Toggle"))
                                    add(msgClickRight + cmp(msgCopyLink))
                                    add(msgButton + Component.keybind("key.hotbar.1", cHighlight) + cmp(" ≫ Remove"))
                                    if (data.maxAmount != null) add(msgButton + Component.keybind("key.hotbar.2", cHighlight) + cmp(" ≫ $msgRiseMaxDownloads"))
                                    if (data.timeout != null) add(msgButton + Component.keybind("key.hotbar.3", cHighlight) + cmp(" ≫ $msgRiseTimeout"))
                                }
                    )
                    setID(100)
                    getOrCreateTagElement("de.miraculixx.api")[idKey] = id
                    setSkullTexture(data.accessType.head.value)
                }, !data.disabled)
            }
        }
    }
}