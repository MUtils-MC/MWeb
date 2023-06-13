package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.cMark
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.web.WebServer
import io.ktor.util.network.*
import net.kyori.adventure.text.event.ClickEvent
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Player

object GlobalListener {
    private val onConnect = Events.Player.postLogin.listen { event ->
        val player = event.player
        val ip = player.connection.remoteAddress.address
        ServerData.setIpToPlayer(ip, event.player.uuid)
        if (WebServer.isOutdated && event.player.hasPermissions(3)) {
            player.sendMessage(prefix + cmp("You are running an outdated version of MWeb!"))
            player.sendMessage(prefix + WebServer.outdatedMessage)
            player.sendMessage(prefix + cmp("Click ") + cmp("here", cMark).clickEvent(ClickEvent.openUrl("https://modrinth.com/mod/mweb")) + cmp(" to install the newest version."))
        }
    }
}