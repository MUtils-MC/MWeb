package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.api.data.AccessUpload
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.io.File

class ItemWhitelists(private val file: File?, private val download: Boolean) : ItemProvider {
    private val msgDot = cmp("  • ", NamedTextColor.DARK_GRAY)
    private val msgButton = cmp(msgString("common.button") + " ", cHighlight)
    private val msgRiseMaxDownloads = msgString("event.action.riseDownloads")
    private val msgRiseTimeout = msgString("event.action.riseTimeout")
    private val msgCopyLink = msgString("event.action.copyLink")

    val idKey = NamespacedKey("de.miraculixx.api", "file-whitelist-id")

    override fun getBooleanMap(from: Int, to: Int): Map<ItemStack, Boolean> {
        return buildMap {
            val whitelists = file?.let { f -> if (download) ServerData.getWhitelists(f.path) else ServerData.getUploads(f.path) } ?: if (download) ServerData.getWhitelists() else ServerData.getUploads()
            whitelists.forEach { (id, data) ->
                val file = File(data.path)
                put(itemStack(Material.PLAYER_HEAD) {
                    meta {
                        name = cmp(file.name, cHighlight)
                        lore(
                            listOf(cmp(file.path, NamedTextColor.DARK_GRAY)) +
                                    data.fullLore(msgDot) + listOf(
                                msgDot + cmp(data.disabled.not().msg()),
                                emptyComponent(),
                                msgClickLeft + cmp("Toggle"),
                                msgClickRight + cmp(msgCopyLink),
                                msgButton + Component.keybind("key.hotbar.1", cHighlight) + cmp(" ≫ Remove")
                            ) + if (data is AccessDownload) getDownloadLore(data) else if (data is AccessUpload) getUploadLore(data) else emptyList()
                        )
                        customModel = 100
                        persistentDataContainer.set(idKey, PersistentDataType.STRING, id)
                    }
                    itemMeta = (itemMeta as SkullMeta).skullTexture(data.accessType.head.value)
                }, !data.disabled)
            }
        }
    }

    private fun getDownloadLore(data: AccessDownload): List<Component> {
        return buildList {
            if (data.maxAmount != null) add(msgButton + Component.keybind("key.hotbar.2", cHighlight) + cmp(" ≫ $msgRiseMaxDownloads"))
            if (data.timeout != null) add(msgButton + Component.keybind("key.hotbar.3", cHighlight) + cmp(" ≫ $msgRiseTimeout"))
        }
    }

    private fun getUploadLore(data: AccessUpload): List<Component> {
        return buildList {
            if (data.timeout != null) add(msgButton + Component.keybind("key.hotbar.3", cHighlight) + cmp(" ≫ $msgRiseTimeout"))
        }
    }
}