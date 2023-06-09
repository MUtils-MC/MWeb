package de.miraculixx.mweb.gui.logic.data

import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.event.GUICloseEvent

interface GUIEvent {
    val run: (GUIClickEvent, CustomInventory) -> Unit
    val close: ((GUICloseEvent, CustomInventory) -> Unit)?
        get() = null
}