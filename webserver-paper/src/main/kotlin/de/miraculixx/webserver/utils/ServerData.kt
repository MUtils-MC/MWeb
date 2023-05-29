package de.miraculixx.webserver.utils

import de.miraculixx.webserver.utils.serializer.UUIDSerializer
import de.miraculixx.webserver.web.WebServer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerLoginEvent
import java.io.File
import java.util.UUID

object ServerData {
    private val dataFile = File(configFolder, "web-data.json")
    private var webData = WebData()

    private val onConnect = listen<PlayerLoginEvent> {
        webData.ipList[it.address.hostAddress] = it.player.uniqueId
    }

    fun ipToPlayer(ip: String): String? {
        return webData.ipList[ip]?.let { Bukkit.getOfflinePlayer(it).name }
    }

    fun getWhitelistedFiles(): MutableSet<String> {
        return webData.whitelistedFiles
    }

    fun saveData() {
        dataFile.writeText(WebServer.json.encodeToString(webData))
    }

    fun loadData() {
        if (!dataFile.exists()) return
        webData = WebServer.json.decodeFromString<WebData>(dataFile.readText().ifBlank { "{}" })
    }

    @Serializable
    private data class WebData(
        val ipList: MutableMap<String, @Serializable(with = UUIDSerializer::class) UUID> = mutableMapOf(),
        val whitelistedFiles: MutableSet<String> = mutableSetOf()
    )
}