package de.miraculixx.mweb.gui.actions

import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent

class ActionEmpty : GUIEvent {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, _: CustomInventory ->
        it.isCancelled = true
    }
}