package de.miraculixx.mweb.gui.items

import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.msgString
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ItemLoading : ItemProvider {
    private val msgLoading = cmp(msgString("items.loading.n"))

    private var step = 0

    override fun getSlotMap(): Map<Int, ItemStack> {
        if (step >= 7) step = 0
        else step++

        val phGreen = itemStack(Material.LIME_STAINED_GLASS_PANE) { meta { name = msgLoading } }
        val phRed = itemStack(Material.RED_STAINED_GLASS_PANE) { meta { name = msgLoading } }
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