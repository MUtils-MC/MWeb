package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.ServerData
import net.axay.kspigot.event.listen
import org.bukkit.event.player.PlayerLoginEvent

object GlobalListener {
    private val onConnect = listen<PlayerLoginEvent> {
        ServerData.setIpToPlayer(it.address.hostAddress, it.player.uniqueId)
    }
}