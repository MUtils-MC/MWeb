package de.miraculixx.mweb.gui.logic.item

import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mvanilla.messages.emptyComponent
import de.miraculixx.mvanilla.messages.msg
import de.miraculixx.mvanilla.messages.msgList
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setSkullTexture
import net.silkmc.silk.nbt.dsl.nbtCompound

object InventoryUtils {
    val phPrimary = itemStack(Items.GRAY_STAINED_GLASS_PANE) { setName(emptyComponent()) }

    fun getCustomItem(key: String, id: Int, texture: Head64): ItemStack {
        return itemStack(Items.PLAYER_HEAD) {
            setSkullTexture(texture.value)
            setName(msg("items.general.$key.n"))
            setLore(msgList("items.general.$key.l", inline = "<grey>"))
            setID(id)
        }
    }

    fun ItemStack.setID(id: Int) {
        addTagElement("de.miraculixx.api", nbtCompound { put("ID", id) })
    }

    fun ItemStack.getID(): Int {
        return getTagElement("de.miraculixx.api")?.getInt("ID") ?: 0
    }

    fun ItemStack.clone(type: Item): ItemStack {
        return itemStack(type) {
            tag = this@clone.tag
        }
    }
}