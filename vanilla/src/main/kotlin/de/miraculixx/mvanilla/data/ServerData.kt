package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.serializer.UUIDSerializer
import de.miraculixx.mvanilla.web.WebServer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.*

object ServerData {
    private val dataFile = File(configFolder, "web-data.json")
    private var webData = WebData()

    fun setIpToPlayer(ip: String, uuid: UUID) {
        webData.ipList[ip] = uuid
    }

    fun ipToPlayer(ip: String): String? {
        return webData.ipList[ip]?.toString()
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