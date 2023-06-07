package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import org.bukkit.entity.Player

fun Player.permVisual(perm: String, close: Boolean = false): Boolean {
    if (hasPermission(perm)) return true
    if (close) closeInventory()
    soundError()
    sendMessage(prefix + cmp(msgString("event.noPermission", listOf(perm)), cError))
    return false
}