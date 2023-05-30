package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.serializer.UUIDSerializer
import de.miraculixx.mvanilla.web.LoaderSpecific
import de.miraculixx.mvanilla.web.WebServer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.*

object ServerData {
    private val dataFile = File(configFolder, "web-data.json")
    private var webData = WebData()

    fun addWhitelist(id: String, data: WhitelistFile): Boolean {
        if (webData.whitelistedFiles.containsKey(id)) return false
        webData.whitelistedFiles[id] = data
        return true
    }

    fun removeWhitelist(id: String): Boolean {
        return webData.whitelistedFiles.remove(id) != null
    }

    /**
     * @return <ID, Path>
     */
    fun getWhitelists(): List<Pair<String, String>> {
        return webData.whitelistedFiles.map { it.key to it.value.path }
    }

    fun setIpToPlayer(ip: String, uuid: UUID) {
        webData.ipList[ip] = uuid
    }

    fun ipToPlayer(ip: String): UUID? {
        return webData.ipList[ip]
    }

    fun hasAccess(ip: String, id: String, passphrase: String?): Boolean {
        val file = webData.whitelistedFiles[id] ?: return false

        return when (file.accessType) {
            WhitelistType.GLOBAL -> true
            WhitelistType.USER_RESTRICTED -> ipToPlayer(ip)?.toString() == file.restriction
            WhitelistType.PASSPHRASE_RESTRICTED -> passphrase == file.restriction
        }
    }

    fun getFileData(id: String): WhitelistFile? {
        return webData.whitelistedFiles[id]
    }

    fun saveData() {
        dataFile.writeText(WebServer.json.encodeToString(webData))
    }

    fun loadData() {
        if (!dataFile.exists()) return
        webData = WebServer.json.decodeFromString<WebData>(dataFile.readText().ifBlank { "{}" })
    }

    /**
     * @param ipList Map ips to players <IP, UUID>
     * @param whitelistedFiles Save file access by ID to [WhitelistFile]. The id is used to download it
     */
    @Serializable
    private data class WebData(
        val ipList: MutableMap<String, @Serializable(with = UUIDSerializer::class) UUID> = mutableMapOf(),
        val whitelistedFiles: MutableMap<String, WhitelistFile> = mutableMapOf()
    )
}