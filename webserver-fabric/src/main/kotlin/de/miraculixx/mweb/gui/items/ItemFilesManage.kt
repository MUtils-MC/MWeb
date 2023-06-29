package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.api.data.AccessData
import de.miraculixx.mweb.gui.getItem
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
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.time.Instant

class ItemFilesManage(startFolder: File, private val guiType: GUITypes) : ItemProvider {
    private val msgNavBack = cmp(msgString("items.navigateBack.n"), cHighlight)
    private val msgButton = cmp(msgString("common.button") + " ", cHighlight)
    private val msgNone = msgString("common.none")
    private val msgDot = cmp("  • ", NamedTextColor.DARK_GRAY)
    private val loreInfo = listOf(emptyComponent(), cmp("• ", NamedTextColor.DARK_GRAY, bold = true) + cmp("Info", cHighlight, underlined = true))
    private val loreWhitelists = listOf(emptyComponent(), cmp("• ", NamedTextColor.DARK_GRAY, bold = true) + cmp("File Access", cHighlight, underlined = true))
    private val loreUploads = listOf(emptyComponent(), cmp("• ", NamedTextColor.DARK_GRAY, bold = true) + cmp("Uploads", cHighlight, underlined = true))

    private val msgNavigate = msgString("event.action.navigate")
    private val msgCreateGlobal = if (guiType != GUITypes.FILE_MANAGE) msgString("event.action.createGlobal") else ""
    private val msgCreatePrivate = if (guiType != GUITypes.FILE_MANAGE) msgString("event.action.createPrivate") else ""
    private val msgCreateCustom = if (guiType != GUITypes.FILE_MANAGE) msgString("event.action.createCustom") else ""
    private val msgManageLinks = if (guiType != GUITypes.FILE_MANAGE) msgString("event.action.manageLinks") else ""
    private val msgRename = if (guiType == GUITypes.FILE_MANAGE) msgString("event.action.rename") else ""
    private val msgZIP = if (guiType == GUITypes.FILE_MANAGE) msgString("event.action.zip") else ""
    private val msgUnZIP = if (guiType == GUITypes.FILE_MANAGE) msgString("event.action.unzip") else ""

    var currentFolder = startFolder
    val pathNamespace = "file-path"

    override fun getItemList(from: Int, to: Int): List<ItemStack> {
        val folderFiles = currentFolder.listFiles()
        val files = folderFiles?.slice(from..to.coerceAtMost(folderFiles.size - 1))?.sortedByDescending { it.isDirectory } ?: emptyList()

        return buildList {
            val parentFile = currentFolder.parentFile
            if (parentFile != null) {
                add(itemStack(Items.PLAYER_HEAD) {
                    setName(msgNavBack)
                    setLore(listOf(cmp(parentFile.path)))
                    setID(99)
                    setSkullTexture(Head64.ARROW_CURLY_LEFT_WHITE.value)
                })
            }

            if (files.isEmpty()) {
                add(itemStack(Items.BARRIER) {
                    setName(cmp("✖", cError))
                })
                return@buildList
            }

            files.forEach { file ->
                val type = if (file.isDirectory) FileType.FOLDER else FileType.getType(file.extension)
                val item = type.getItem()
                item.setName(cmp(file.name, cHighlight))
                item.setLore(
                    listOf(cmp(FileType.formatPath(file.path), NamedTextColor.DARK_GRAY)) +
                            file.getLore(type)
                )
                item.setID(100)
                item.getOrCreateTagElement("de.miraculixx.api")[pathNamespace] = file.path
                add(item)
            }
            if (parentFile != null && files.size >= to - from) removeLast()
        }
    }

    private fun File.getLore(type: FileType): List<Component> {
        val isFolder = isDirectory
        return when (guiType) {
            GUITypes.FILE_MANAGE -> buildList {
                addAll(loreInfo)
                add(msgDot + cmp(type.desc))
                add(msgDot + cmp(FileType.getTime(Instant.ofEpochMilli(lastModified()))))
                if (!isFolder) add(msgDot + cmp(FileUtils.byteCountToDisplaySize(Files.size(toPath()))))
                add(emptyComponent())
                if (isFolder) add(msgClickLeft + cmp(msgNavigate))
                add(msgButton + Component.keybind("key.hotbar.1", cHighlight) + cmp(" ≫ $msgRename"))
                add(msgButton + Component.keybind("key.hotbar.2", cHighlight) + cmp(" ≫ $msgZIP"))
                add(msgButton + Component.keybind("key.hotbar.3", cHighlight) + cmp(" ≫ Delete ${if (isFolder) "Recursively" else ""}"))
                if (type == FileType.ARCHIVE) add(msgButton + Component.keybind("key.hotbar.4", cHighlight) + cmp(" ≫ $msgUnZIP"))
            }

            GUITypes.FILE_WHITELISTING -> buildList {
                addAll(loreWhitelists)
                addAll(getAccessLore(ServerData.getWhitelists(path)))
            }
            GUITypes.FILE_UPLOADING -> buildList {
                if (!isDirectory) return@buildList
                addAll(loreUploads)
                addAll(getAccessLore(ServerData.getUploads(path)))
            }

            else -> emptyList()
        }
    }

    private fun File.getAccessLore(data: Map<String, AccessData>): List<Component> {
        return buildList {
            when (data.size) {
                0 -> add(msgDot + cmp(msgNone, italic = true))
                1 -> {
                    val key = data.keys.first()
                    val whitelist = data[key] ?: return@buildList
                    addAll(whitelist.fullLore(msgDot))
                }
                else -> {
                    data.forEach { (_, data) ->
                        add(data.compactLore(msgDot))
                    }
                }
            }
            add(emptyComponent())
            if (isDirectory) add(msgClickLeft + cmp(msgNavigate))
            add(msgButton + Component.keybind("key.hotbar.1", cHighlight) + cmp(" ≫ $msgCreateGlobal"))
            add(msgButton + Component.keybind("key.hotbar.2", cHighlight) + cmp(" ≫ $msgCreatePrivate"))
            add(msgButton + Component.keybind("key.hotbar.3", cHighlight) + cmp(" ≫ $msgCreateCustom"))
            if (data.isNotEmpty()) add(msgButton + Component.keybind("key.hotbar.4", cHighlight) + cmp(" ≫ $msgManageLinks"))
        }
    }

    override fun getExtra(): List<ItemStack> {
        return listOf(
            getHeader(1, cmp(msgString("items.fileManage.n"), cHighlight), if (guiType == GUITypes.FILE_MANAGE) Head64.HASHTAG_LIGHT_BLUE else Head64.HASHTAG_BLUE),
            getHeader(2, cmp(msgString("items.fileWhitelist.n"), cHighlight), if (guiType == GUITypes.FILE_WHITELISTING) Head64.ARROW_DOWN_LIGHT_BLUE else Head64.ARROW_DOWN_BLUE),
            getHeader(3, cmp(msgString("items.fileUpload.n"), cHighlight), if (guiType == GUITypes.FILE_UPLOADING) Head64.PLUS_LIGHT_BLUE else Head64.PLUS_BLUE)
        )
    }

    private fun getHeader(id: Int, display: Component, head: Head64): ItemStack {
        return itemStack(Items.PLAYER_HEAD) {
            setName(display)
            setID(id)
            setSkullTexture(head.value)
        }
    }
}