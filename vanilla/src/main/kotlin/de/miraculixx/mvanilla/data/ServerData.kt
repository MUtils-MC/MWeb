package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.serializer.UUIDSerializer
import de.miraculixx.mvanilla.web.WebServer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.io.path.Path
import java.util.*

object ServerData {
    private val dataFile = Path(configFolder, "web-data.json")
    private var webData = WebData()

    fun addWhitelist(id: String, data: WhitelistFile): Boolean {
        if (webData.whitelistedFiles.containsKey(id)) return false
        webData.whitelistedFiles[id] = data
        return true
    }

    fun removeWhitelist(id: String): Boolean {
        val data = webData.whitelistedFiles.remove(id) ?: return false
        data.zippedTo?.let { Path(it).delete() }
        return true
    }

    /**
     * @return <ID, Data>
     */
    fun getWhitelists() =
        webData.whitelistedFiles

    /**
     * @return <ID, Data>
     */
    fun getWhitelists(path: String) =
        webData.whitelistedFiles.filter { it.value.path == path }

    fun checkPlayer(uuid: UUID) =
        webData.ipList.containsValue(uuid)

    fun setIpToPlayer(ip: String, uuid: UUID) {
        webData.ipList[ip] = uuid
    }

    fun ipToPlayer(ip: String) =
        webData.ipList[ip]

    fun hasAccess(ip: String, id: String, passphrase: String?): Boolean {
        val file = webData.whitelistedFiles[id] ?: return false

        return when (file.accessType) {
            WhitelistType.GLOBAL -> true
            WhitelistType.USER_RESTRICTED -> ipToPlayer(ip)?.toString() == file.restriction
            WhitelistType.PASSPHRASE_RESTRICTED -> passphrase == file.restriction
        }
    }

    fun isUnavailable(fileData: WhitelistFile): Boolean {
        return fileData.disabled ||
                (fileData.timeout != null && System.currentTimeMillis() >= fileData.timeout!!) ||
                (fileData.maxAmount != null && fileData.requestAmount >= fileData.maxAmount!!)
    }

    fun getFileData(id: String) =
        webData.whitelistedFiles[id]

    fun getLink(id: String): String {
        val data = getFileData(id)
        val extension = if (data?.accessType == WhitelistType.PASSPHRASE_RESTRICTED) "?pw=${data.restriction}" else ""
        return settings.proxy?.let { "$it/d/$id$extension" } ?: "http://${WebServer.publicIP}:${settings.port}/d/$id$extension"
    }

    fun saveData() {
        dataFile.writeText(WebServer.jsonFull.encodeToString(webData))
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