package de.miraculixx.mweb.gui.logic

import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mvanilla.data.Head64
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object InventoryUtils {
    val phPrimary = itemStack(Material.GRAY_STAINED_GLASS_PANE) { meta { name = emptyComponent() } }
    val phSecondary = itemStack(Material.BLACK_STAINED_GLASS_PANE) { meta { name = emptyComponent() } }

    fun getCustomItem(key: String, id: Int, texture: Head64): ItemStack {
        return itemStack(Material.PLAYER_HEAD) {
            meta<SkullMeta> {
                skullTexture(texture.value)
                displayName(msg("items.general.$key.n"))
                lore(msgList("items.general.$key.l", inline = "<grey>"))
                customModel = id
            }
        }
    }

    fun FileConfiguration.toggle(key: String, inventory: CustomInventory, player: Player) {
        if (getBoolean(key)) {
            set(key, false)
            player.soundDisable()
        } else {
            set(key, true)
            player.soundEnable()
        }
        inventory.update()
    }

    fun FileConfiguration.numberChanger(player: Player, click: ClickType, key: String, inv: CustomInventory, step: Int = 1, min: Int = 0, max: Int = 100) {
        val up = when (click) {
            ClickType.LEFT -> true
            ClickType.RIGHT -> false
            else -> return
        }
        settings(up, player, key, inv, step.toDouble(), max.toDouble(), min.toDouble())
    }

    fun FileConfiguration.numberChangerShift(player: Player, click: ClickType, key: String, inv: CustomInventory, step: Int = 1, min: Int = 0, max: Int = 100) {
        val up = when (click) {
            ClickType.RIGHT -> true
            ClickType.SHIFT_RIGHT -> false
            else -> return
        }
        settings(up, player, key, inv, step.toDouble(), max.toDouble(), min.toDouble())
    }

    fun FileConfiguration.numberChangerShift(player: Player, click: ClickType, key: String, inv: CustomInventory, step: Double = 1.0, min: Double = 0.0, max: Double = 100.0) {
        val up = when (click) {
            ClickType.RIGHT -> true
            ClickType.SHIFT_RIGHT -> false
            else -> return
        }
        settings(up, player, key, inv, step, max, min)
    }

    private fun FileConfiguration.settings(up: Boolean, player: Player, key: String, inv: CustomInventory, step: Double, max: Double, min: Double) {
        if (up) {
            if (getDouble(key) >= max) {
                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                return
            }
            this[key] = getDouble(key) + step
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f)
        } else {
            if (getDouble(key) <= min) {
                this[key] = min
                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                return
            }
            this[key] = getDouble(key) - step
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 0.5f)
        }
        inv.update()
    }

    fun PersistentDataContainer?.get(namespacedKey: NamespacedKey): String? {
        return this?.get(namespacedKey, PersistentDataType.STRING)
    }
}
