package de.miraculixx.mweb.gui

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.SkullMeta

fun FileType.getItem(): ItemStack {
    return when (this) {
        FileType.FOLDER -> itemStack(Material.PLAYER_HEAD) { itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.CHEST.value) }
        FileType.ARCHIVE -> itemStack(Material.PLAYER_HEAD) { itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.WINRAR.value) }
        FileType.JAR -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.PURPLE } }
        FileType.CONFIGURATION -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.GREEN } }
        FileType.DANGEROUS -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.RED } }
        FileType.MC_FILES -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.ORANGE } }
        FileType.MEDIA_FILES -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.AQUA } }
        FileType.DATA -> itemStack(Material.FILLED_MAP) { meta<MapMeta> { color = Color.GRAY } }
    }
}