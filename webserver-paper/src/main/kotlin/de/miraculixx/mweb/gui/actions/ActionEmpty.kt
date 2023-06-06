package de.miraculixx.mweb.gui.actions

import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import org.bukkit.event.inventory.InventoryClickEvent

class ActionEmpty: GUIEvent {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, _: CustomInventory ->
        it.isCancelled = true
    }
}