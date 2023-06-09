package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.web.LoaderSpecific
import de.miraculixx.mweb.adventure
import de.miraculixx.mweb.server
import net.kyori.adventure.text.Component
import java.util.*

class LoaderImplementation : LoaderSpecific() {
    init {
        INSTANCE = this
    }

    override fun uuidToPlayerName(uuid: UUID): String {
        return server.playerList.getPlayer(uuid)?.scoreboardName ?: uuid.toString()
    }
}