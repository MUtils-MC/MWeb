package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.serializer.UUIDSerializer
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.api.data.AccessUpload
import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.api.data.WhitelistType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.*

object ServerData {
    private val dataFile = File(configFolder, "web-data.json")
    private var webData = WebData()

    fun addWhitelist(id: String, data: AccessDownload): Boolean {
        if (webData.whitelistedFiles.containsKey(id)) return false
        webData.whitelistedFiles[id] = data
        return true
    }

    fun addUpload(id: String, data: AccessUpload): Boolean {
        if (webData.uploadFolders.containsKey(id)) return false
        webData.uploadFolders[id] = data
        return true
    }

    fun removeWhitelist(id: String): Boolean {
        val data = webData.whitelistedFiles.remove(id) ?: return false
        data.zippedTo?.let { File(it).delete() }
        return true
    }

    fun removeUpload(id: String) =
        webData.uploadFolders.remove(id) != null


    /**
     * @return <ID, Data>
     */
    fun getWhitelists() = webData.whitelistedFiles

    fun getUploads() = webData.uploadFolders

    /**
     * @return <ID, Data>
     */
    fun getWhitelists(path: String) =
        webData.whitelistedFiles.filter { it.value.path == path }

    fun getUploads(path: String) =
        webData.uploadFolders.filter { it.value.path == path }

    fun checkPlayer(uuid: UUID) =
        webData.ipList.containsValue(uuid)

    fun setIpToPlayer(ip: String, uuid: UUID) {
        webData.ipList[ip] = uuid
    }

    fun ipToPlayer(ip: String) =
        webData.ipList[ip]

    fun fileHasAccess(ip: String, id: String, passphrase: String?): Boolean {
        val file = webData.whitelistedFiles[id] ?: return false
        return hasAccess(ip, file.accessType, passphrase, file.restriction)
    }

    fun uploadHasAccess(ip: String, id: String, passphrase: String?): Boolean {
        val upload = webData.uploadFolders[id] ?: return false
        return hasAccess(ip, upload.accessType, passphrase, upload.restriction)
    }

    private fun hasAccess(ip: String, accessType: WhitelistType, passphrase: String?, restriction: String?): Boolean {
        return when (accessType) {
            WhitelistType.GLOBAL -> true
            WhitelistType.USER_RESTRICTED -> ipToPlayer(ip)?.toString() == restriction
            WhitelistType.PASSPHRASE_RESTRICTED -> passphrase == restriction
        }
    }

    fun isFileUnavailable(fileData: AccessDownload): Boolean {
        return fileData.disabled ||
                (fileData.timeout != null && System.currentTimeMillis() >= fileData.timeout!!) ||
                (fileData.maxAmount != null && fileData.requestAmount >= fileData.maxAmount!!)
    }

    fun isUploadUnavailable(uploadData: AccessUpload): Boolean {
        return uploadData.disabled ||
                (uploadData.timeout != null && System.currentTimeMillis() >= uploadData.timeout!!)
    }

    fun getFileData(id: String) =
        webData.whitelistedFiles[id]

    fun getUploadData(id: String) =
        webData.uploadFolders[id]

    fun getLink(id: String, download: Boolean): String {
        val data = if (download) getFileData(id) else getUploadData(id)
        val sep = if (download) "d" else "u"
        val extension = if (data?.accessType == WhitelistType.PASSPHRASE_RESTRICTED) "?pw=${data.restriction}" else ""
        return settings.proxy?.let { "$it/$sep/$id$extension" } ?: "http://${WebServer.publicIP}:${settings.port}/$sep/$id$extension"
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
     * @param whitelistedFiles Save file access by ID to [AccessDownload]. The id is used to download it
     */
    @Serializable
    private data class WebData(
        val ipList: MutableMap<String, @Serializable(with = UUIDSerializer::class) UUID> = mutableMapOf(),
        val whitelistedFiles: MutableMap<String, AccessDownload> = mutableMapOf(),
        val uploadFolders: MutableMap<String, AccessUpload> = mutableMapOf()
    )
}