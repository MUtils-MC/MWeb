package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.messages.soundStone
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import net.axay.kspigot.items.customModel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesUpload : GUIEvent, ActionFiles {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.player as? ServerPlayer ?: return@event
        val item = it.item
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (item.getID()) {
            100 -> {
                val path =  item.getTagElement("de.miraculixx.api")?.getString(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)

                when (it.click) {
                    GUIClick.LEFT_CLICK -> file.navigate(player, provider, inv)

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.openManager(provider)
            2 -> player.openWhitelist(provider)
            3 -> player.soundStone()
        }
    }
}