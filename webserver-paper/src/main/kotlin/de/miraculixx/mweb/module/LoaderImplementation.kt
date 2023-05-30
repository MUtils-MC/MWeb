package de.miraculixx.mweb.module

import de.miraculixx.mvanilla.web.LoaderSpecific
import org.bukkit.Bukkit
import java.util.*

class LoaderImplementation: LoaderSpecific() {
    init {
        INSTANCE = this
    }

    override fun uuidToPlayerName(uuid: UUID): String {
        return Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString()
    }
}