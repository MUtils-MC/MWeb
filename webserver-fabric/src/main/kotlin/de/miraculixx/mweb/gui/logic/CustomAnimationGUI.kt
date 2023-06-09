package de.miraculixx.mweb.gui.logic

import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.cError
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.InventoryManager
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.event.GUICloseEvent
import de.miraculixx.mweb.gui.logic.item.setName
import kotlinx.coroutines.cancel
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.infiniteMcCoroutineTask

class CustomAnimationGUI(
    override val itemProvider: ItemProvider?,
    title: Component,
    override val id: String,
    players: List<Player>,
    size: Int,
    private val period: Int,
    clickEvent: ((GUIClickEvent, CustomInventory) -> Unit)?,
    closeEvent: ((GUICloseEvent, CustomInventory) -> Unit)?
) : CustomInventory(size * 9, title, clickEvent, closeEvent) {
    override val defaultClickAction: ((GUIClickEvent, CustomInventory) -> Unit)? = null
    private val i = get()

    private constructor(builder: Builder) : this(
        builder.itemProvider,
        builder.title,
        builder.id,
        buildList {
            addAll(builder.players)
            builder.player?.let { add(it) }
        },
        builder.size,
        builder.period,
        builder.clickAction,
        builder.closeAction
    )

    class Builder(val id: String) : CustomInventory.Builder() {
        /**
         * Sets the inventory size. It defines the row count, [size] 2 will create a GUI with 18 slots (2 rows)
         */
        var size: Int = 1

        var period: Int = 20

        /**
         * Internal use. No need to call it inlined
         */
        fun build(): CustomAnimationGUI = CustomAnimationGUI(this)
    }

    override fun update() {
        val content = itemProvider?.getSlotMap()
        val maxSize = i.containerSize
        content?.forEach { (slot, item) ->
            if (slot >= maxSize) return
            i.setItem(slot, item)
        }
    }

    private fun fillPlaceholder() {
        val primaryPlaceholder = itemStack(Items.GRAY_STAINED_GLASS_PANE) { setName(cmp(" ")) }
        val secondaryPlaceholder = itemStack(Items.BLACK_STAINED_GLASS_PANE) { setName(cmp(" ")) }

        val size = i.containerSize
        repeat(size) {
            i.setItem(it, primaryPlaceholder)
        }
        if (size != 9) {
            i.setItem(17, secondaryPlaceholder)
            i.setItem(size - 18, secondaryPlaceholder)
            repeat(2) { i.setItem(it, secondaryPlaceholder) }
            repeat(3) { i.setItem(it + 7, secondaryPlaceholder) }
            repeat(3) { i.setItem(size - it - 8, secondaryPlaceholder) }
            repeat(2) { i.setItem(size - it - 1, secondaryPlaceholder) }
        } else {
            i.setItem(0, secondaryPlaceholder)
            i.setItem(8, secondaryPlaceholder)
        }
    }

    private val scheduler = infiniteMcCoroutineTask(false, period = period.ticks) {
        if (viewers.isEmpty()) {
            this.cancel()
            return@infiniteMcCoroutineTask
        }
        update()
    }

    init {
        if (players.isEmpty()) {
            consoleAudience.sendMessage(prefix + cmp("Creating GUI without player - Unexpected behaviour", cError))
            InventoryManager.remove(id)
        } else {
            fillPlaceholder()
            update()
            open(players)
        }
    }
}