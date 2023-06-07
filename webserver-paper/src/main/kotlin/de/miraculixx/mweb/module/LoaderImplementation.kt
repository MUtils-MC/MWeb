package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.messages.soundError
import de.miraculixx.mvanilla.web.LoaderSpecific
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class LoaderImplementation: LoaderSpecific() {
    init {
        INSTANCE = this
    }

    override fun uuidToPlayerName(uuid: UUID): String {
        return Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString()
    }
}