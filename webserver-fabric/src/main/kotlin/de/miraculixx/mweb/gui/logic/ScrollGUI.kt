package de.miraculixx.mweb.gui.logic

import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.InventoryManager
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.event.GUICloseEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.setID
import de.miraculixx.mweb.gui.logic.item.adv
import de.miraculixx.mweb.gui.logic.item.setLore
import de.miraculixx.mweb.gui.logic.item.setName
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.nbt.set

class ScrollGUI(
    override val itemProvider: ItemProvider?,
    override val id: String,
    title: Component,
    players: List<Player>,
    startPage: Int,
    filterable: Boolean,
    private val dataKeys: List<String>,
    clickEvent: ((GUIClickEvent, CustomInventory) -> Unit)?,
    closeEvent: ((GUICloseEvent, CustomInventory) -> Unit)?,
) : CustomInventory(4 * 9, title, clickEvent, closeEvent) {
    private val i = get()
    private var page = startPage
    private val arrowRedL = InventoryUtils.getCustomItem("arrowLeftEnd", 9000, Head64.ARROW_LEFT_RED)
    private val arrowRedR = InventoryUtils.getCustomItem("arrowRightEnd", 9000, Head64.ARROW_RIGHT_RED)
    private val arrowGreenL = InventoryUtils.getCustomItem("arrowLeft", 9001, Head64.ARROW_LEFT_GREEN)
    private val arrowGreenR = InventoryUtils.getCustomItem("arrowRight", 9002, Head64.ARROW_RIGHT_GREEN)
    private val pageIndicator = itemStack(Items.KNOWLEDGE_BOOK) {
        setLore(msgList("items.general.pageIndicator.l", inline = "<grey>"))
        setID(9003)
    }
    private val activated = cmp(msgString("common.boolTrue"), cSuccess)
    private val deactivated = cmp(msgString("common.boolFalse"), cError)
    override val defaultClickAction: ((GUIClickEvent, CustomInventory) -> Unit) = action@{ it: GUIClickEvent, inv: CustomInventory ->
        val item = it.item
        val player = it.player
        val click = it.click
        if (it.slot == -999) {
            if (click.isLeftClick()) {
                if (it.gui.getItem(35).getID() == 9000) return@action
                page += 1
            } else if (click.isRightClick()) {
                if (page == 0) return@action
                else page -= 1
            } else return@action
            player.adv().click()
            update()
            return@action
        }

        when (item.getID()) {
            9000 -> {
                it.isCancelled = true
                player.adv().playSound(Sound.sound(Key.key("block.stone.hit"), Sound.Source.BLOCK, 1f, 1f))
            }

            9001 -> {
                it.isCancelled = true
                page = (page - if (it.click.isShiftClick()) 5
                else 1).coerceAtLeast(0)
                player.adv().click()
                update()
            }

            9002 -> {
                it.isCancelled = true
                page += if (it.click.isShiftClick()) 5 else 1
                player.adv().click()
                update()
            }

            9003 -> {
                it.isCancelled = true
                player.adv().click()
                InventoryManager.storageBuilder("$id-STORAGE") {
                    this.title = title
                    this.players = viewers.keys.toList()
                    this.clickAction = clickEvent
                    this.closeAction = closeEvent
                    this.itemProvider = this@ScrollGUI.itemProvider
                    this.scrollable = true
                    this.filterable = filterable
                }
            }
        }
    }

    private constructor(builder: Builder) : this(
        builder.itemProvider,
        builder.id,
        builder.title,
        buildList {
            addAll(builder.players)
            builder.player?.let { add(it) }
        },
        builder.startPage,
        builder.filterable,
        builder.dataKeys,
        builder.clickAction,
        builder.closeAction
    )

    class Builder(val id: String) : CustomInventory.Builder() {
        /**
         * Define the startpage for this GUI. Only items matching the current page are visible.
         *
         * Default: **0**
         */
        var startPage: Int = 0

        /**
         * Defines the data container keys that should be copied to the signal item
         */
        var dataKeys: List<String> = emptyList()

        /**
         * Pass through the value to [StorageGUI], if the storage view is used
         */
        var filterable: Boolean = false

        /**
         * Internal function
         */
        fun build() = ScrollGUI(this)
    }

    override fun update() {
        val lastIndex = page + 7
        val content = itemProvider?.getBooleanMap(page, lastIndex) ?: emptyMap() // Render only visible items

        // Clean up
        fillPlaceholder(false)

        // Adding Basic Buttons
        i.setItem(27, if (page == 0) arrowRedL else arrowGreenL)
        i.setItem(35, if (content.size < 7) arrowRedR else arrowGreenR)
        pageIndicator.count = (page + 1).coerceIn(1..64)
        pageIndicator.setName((cmp("Page ${page + 1}", cHighlight)))
        i.setItem(31, pageIndicator)

        // Adding Content
        content.toList().forEachIndexed { index, data ->
            if (data.second) {
                data.first.enchant(Enchantments.MENDING, 1)
                data.first.applyComponents(DataComponentPatch.builder().set(DataComponents.HIDE_TOOLTIP, net.minecraft.util.Unit.valueOf("true")).build())
            }
            i.setItem(index + 19, itemStack(if (data.second) Items.LIME_STAINED_GLASS_PANE else Items.RED_STAINED_GLASS_PANE) {
                setName(if (data.second) activated else deactivated)
                val sourceMeta = data.first
                setID(sourceMeta.getID())
                val tagContainer = getOrCreateTagElement("de.miraculixx.api")
                dataKeys.forEach { key ->
                    tagContainer[key] = sourceMeta.getTagElement("de.miraculixx.api")?.getString(key) ?: ""
                }
            })
            i.setItem(index + 10, data.first)
        }
    }

    private fun fillPlaceholder(full: Boolean) {
        val primaryPlaceholder = itemStack(Items.GRAY_STAINED_GLASS_PANE) { setName(cmp(" ")) }
        val secondaryPlaceholder = itemStack(Items.BLACK_STAINED_GLASS_PANE) { setName(cmp(" ")) }
        val missingSetting = itemStack(Items.BARRIER) { setName(cmp("✖", cError)) }

        if (full) {
            repeat(i.containerSize) { i.setItem(it, primaryPlaceholder) }
            listOf(0, 1, 7, 8, 9, 17, 18, 26, 27, 28, 34, 35).forEach { i.setItem(it, secondaryPlaceholder) }
        } else (19..25).forEach { i.setItem(it, primaryPlaceholder) }
        (10..16).forEach { i.setItem(it, missingSetting) }
    }

    init {
        fillPlaceholder(true)
        update()
        open(players)
    }
}