package de.miraculixx.mweb.gui

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mweb.api.data.Head64
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setSkullTexture
import net.silkmc.silk.nbt.set

fun FileType.getItem(): ItemStack {
    return when (this) {
        FileType.FOLDER -> itemStack(Items.PLAYER_HEAD) { setSkullTexture(Head64.CHEST.value) }
        FileType.ARCHIVE -> itemStack(Items.PLAYER_HEAD) { setSkullTexture(Head64.WINRAR.value) }
        FileType.JAR -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 11141290 }
        FileType.CONFIGURATION -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 43520 }
        FileType.DANGEROUS -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 11141120 }
        FileType.MC_FILES -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 16755200 }
        FileType.MEDIA_FILES -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 5636095 }
        FileType.DATA -> itemStack(Items.FILLED_MAP) { getOrCreateTagElement("display")["MapColor"] = 5592405 }
    }
}