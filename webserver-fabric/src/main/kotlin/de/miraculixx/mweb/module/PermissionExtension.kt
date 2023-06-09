package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.level.ServerPlayer

fun ServerPlayer.permVisual(perm: String, close: Boolean = false): Boolean {
    if (Permissions.check(this, perm, 4)) return true
    if (close) closeContainer()
    soundError()
    sendMessage(prefix + cmp(msgString("event.noPermission", listOf(perm)), cError))
    return false
}