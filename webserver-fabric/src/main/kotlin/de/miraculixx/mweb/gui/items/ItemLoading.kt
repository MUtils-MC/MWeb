package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.msgString
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.item.setName
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack

class ItemLoading : ItemProvider {
    private val msgLoading = cmp(msgString("items.loading.n"))

    private var step = 0

    override fun getSlotMap(): Map<Int, ItemStack> {
        if (step >= 7) step = 0
        else step++

        val phGreen = itemStack(Items.LIME_STAINED_GLASS_PANE) { setName(msgLoading) }
        val phRed = itemStack(Items.RED_STAINED_GLASS_PANE) { setName(msgLoading) }
        return buildMap {
            (0..6).forEach { slot ->
                put(10 + slot, phRed)
            }
            if (step == 0) return@buildMap
            (0 until step).forEach { slot ->
                put(10 + slot, phGreen)
            }
        }
    }
}