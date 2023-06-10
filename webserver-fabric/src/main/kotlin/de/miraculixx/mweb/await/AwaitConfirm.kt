package de.miraculixx.mweb.await

import de.miraculixx.mvanilla.messages.cSuccess
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.msgString
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.data.InventoryManager
import de.miraculixx.mweb.gui.logic.data.ItemProvider
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.setID
import de.miraculixx.mweb.gui.logic.item.setName
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setSkullTexture

class AwaitConfirm(source: ServerPlayer, onConfirm: () -> Unit, onCancel: () -> Unit) {
    private val gui = InventoryManager.inventoryBuilder("${source.uuid}-CONFIRM") {
        title = cmp("• ") + cmp(msgString("common.confirm"), NamedTextColor.DARK_GREEN)
        size = 3
        player = source
        itemProvider = InternalItemProvider()
        clickAction = InternalClickProvider(source, onConfirm, onCancel, this@AwaitConfirm).run
    }

    private class InternalItemProvider : ItemProvider {
        override fun getSlotMap(): Map<Int, ItemStack> {
            return mapOf(
                12 to itemStack(Items.PLAYER_HEAD) {
                    setID(1)
                    setName(cmp(msgString("common.confirm"), cSuccess))
                    setSkullTexture(Head64.CHECKMARK_GREEN.value)
                },
                14 to itemStack(Items.PLAYER_HEAD) {
                    setID(2)
                    setName(cmp(msgString("common.cancel"), cSuccess))
                    setSkullTexture(Head64.X_RED.value)
                }
            )
        }
    }

    private class InternalClickProvider(player: ServerPlayer, onConfirm: () -> Unit, onCancel: () -> Unit, confirmer: AwaitConfirm) : GUIEvent {
        override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, _: CustomInventory ->
            it.isCancelled = true
            if (it.player != player) return@event

            when (it.item.getID()) {
                1 -> {
                    player.closeContainer()
                    onConfirm.invoke()
                }

                2 -> {
                    player.closeContainer()
                    onCancel.invoke()
                }

                else -> return@event
            }
        }
    }
}