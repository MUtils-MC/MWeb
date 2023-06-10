package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.ServerData
import io.ktor.util.network.*
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Player

object GlobalListener {
    private val onConnect = Events.Player.postLogin.listen { event ->
        val ip = event.player.connection.remoteAddress.address
        println(ip)
        ServerData.setIpToPlayer(ip, event.player.uuid)
    }
}