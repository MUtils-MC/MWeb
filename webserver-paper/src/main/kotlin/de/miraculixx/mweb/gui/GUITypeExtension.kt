package de.miraculixx.mweb.gui

import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.data.InventoryManager
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

fun GUITypes.buildInventory(player: Player, id: String, itemProvider: ItemProvider?, clickAction: GUIEvent) {
    InventoryManager.get(id)?.open(player) ?: when (this) {
        GUITypes.FILE_MANAGE, GUITypes.FILE_UPLOADING, GUITypes.FILE_WHITELISTING -> InventoryManager.storageBuilder(id) {
            this.title = this@buildInventory.title
            this.player = player
            this.filterable = false
            this.scrollable = true
            this.headers = itemProvider?.getExtra() ?: emptyList()
            this.itemProvider = itemProvider
            this.clickAction = clickAction.run
        }

        GUITypes.MANAGE_WHITELISTS -> InventoryManager.scrollBuilder(id) {
            this.title = this@buildInventory.title
            this.player = player
            this.dataKeys = listOf(NamespacedKey("de.miraculixx.api", "file-whitelist-id"))
            this.itemProvider = itemProvider
            this.clickAction = clickAction.run
        }

        GUITypes.LOADING -> InventoryManager.animationBuilder(id) {
            this.title = this@buildInventory.title
            this.player = player
            this.period = 5
            this.size = 3
            this.itemProvider = itemProvider
            this.clickAction = clickAction.run
        }

        GUITypes.CREATE_CUSTOM_WHITELIST -> InventoryManager.inventoryBuilder(id) {
            this.title = this@buildInventory.title
            this.player = player
            this.size = 3
            this.itemProvider = itemProvider
            this.clickAction = clickAction.run
        }
    }
}
