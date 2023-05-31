package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.getItem
import de.miraculixx.mweb.gui.logic.items.Head64
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.apache.commons.io.FileUtils
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.nio.file.Files
import java.time.Instant

class ItemFilesManage(startFolder: File) : ItemProvider {
    private val msgNavBack = cmp(msgString("items.navigateBack.n"))

    var currentFolder = startFolder
    val pathNamespace = NamespacedKey("de.miraculixx.api", "file-path")

    override fun getItemList(from: Int, to: Int): List<ItemStack> {
        val folderFiles = currentFolder.listFiles()
        val files = folderFiles?.slice(from..to.coerceAtMost(folderFiles.size - 1))?.sortedByDescending { it.isDirectory } ?: emptyList()

        return buildList {
            val parentFIle = currentFolder.parentFile
            if (parentFIle != null) {
                add(itemStack(Material.PLAYER_HEAD) {
                    meta {
                        name = msgNavBack
                        lore(listOf(cmp(parentFIle.path)))
                        customModel = 99
                    }
                    itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.ARROW_CURLY_LEFT_WHITE.value)
                })
            }

            if (files.isEmpty()) {
                add(itemStack(Material.BARRIER) {
                    meta {
                        name = cmp("✖", cError)
                    }
                })
                return@buildList
            }

            files.forEach { file ->
                val isFolder = file.isDirectory
                val type = if (isFolder) FileType.FOLDER else FileType.getType(file.extension)
                val item = type.getItem()
                item.editMeta {
                    it.name = cmp(file.name, cHighlight)
                    it.lore(
                        listOf(
                            cmp(file.path, NamedTextColor.DARK_GRAY),
                            cmp("• ${type.desc}", NamedTextColor.DARK_GRAY),
                            cmp("• ${FileUtils.byteCountToDisplaySize(Files.size(file.toPath()))}", NamedTextColor.DARK_GRAY),
                            cmp("• ${FileType.getTime(Instant.ofEpochMilli(file.lastModified()))}", NamedTextColor.DARK_GRAY),
                            emptyComponent(),
                            msgClickLeft + cmp(if (isFolder) "ZIP" else "Add ZIP"),
                            msgClickRight + cmp("Rename"),
                            msgShiftClickRight + cmp("Delete ${if (isFolder) "Recursive" else ""}")
                        )
                    )
                    it.customModel = 100
                    it.persistentDataContainer.set(pathNamespace, PersistentDataType.STRING, file.path)
                }
                add(item)
            }
        }
    }

    override fun getExtra(): List<ItemStack> {
        return listOf(
            getHeader(1, cmp(msgString("items.fileManage.n")), Head64.HASHTAG_BLUE),
            getHeader(2, cmp(msgString("items.fileWhitelist.n")), Head64.ARROW_DOWN_BLUE),
            getHeader(3, cmp(msgString("items.fileUpload.n")), Head64.PLUS_BLUE)
        )
    }

    private fun getHeader(id: Int, display: Component, head: Head64): ItemStack {
        return itemStack(Material.PLAYER_HEAD) {
            meta {
                name = display
                customModel = id
                if (id == 1) {
                    addUnsafeEnchantment(Enchantment.MENDING, 1)
                    addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
            itemMeta = (itemMeta as SkullMeta).skullTexture(head.value)
        }
    }
}