package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.cMark
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.web.WebServer
import net.axay.kspigot.event.listen
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent

object GlobalListener {
    private val onConnect = listen<PlayerLoginEvent> {
        val player = it.player
        ServerData.setIpToPlayer(it.address.hostAddress, player.uniqueId)
    }

    private val onJoin = listen<PlayerJoinEvent> {
        val player = it.player
        if (WebServer.isOutdated && player.hasPermission("mweb.updater")) {
            player.sendMessage(prefix + cmp("You are running an outdated version of MWeb!"))
            player.sendMessage(prefix + WebServer.outdatedMessage)
            player.sendMessage(prefix + cmp("Click ") + cmp("here", cMark).clickEvent(ClickEvent.openUrl("https://modrinth.com/mod/mweb")) + cmp(" to install the newest version."))
        }
    }
}