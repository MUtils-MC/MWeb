package de.miraculixx.mweb.gui.logic.event

import de.miraculixx.mweb.gui.logic.data.CustomInventory
import net.minecraft.world.entity.player.Player

class GUICloseEvent(
    val gui: CustomInventory,
    val player: Player,
)