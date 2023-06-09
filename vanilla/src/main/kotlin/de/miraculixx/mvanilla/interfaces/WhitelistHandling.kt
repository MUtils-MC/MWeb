package de.miraculixx.mvanilla.interfaces

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.api.data.AccessData
import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.api.data.AccessUpload
import de.miraculixx.mweb.api.data.WhitelistType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration

interface WhitelistHandling {
    private val charList: Array<Char>
        get() = arrayOf('a', 'b', 'c', 'e', 'g', 'h', 'k', 'n', 's', 'x')

    /**
     * Whitelist a file to create a download link
     * @param path Path to the file
     * @param access Access type - Restrict file to certain users
     * @param restriction Needed if access type is restricted. UUID, passphrase, ...
     * @param duration Access only for a limited duration. Null = infinity
     */
    fun Audience.whitelistFile(path: String, access: WhitelistType, restriction: String? = null, duration: Duration? = null, maxDownloads: Int? = null): Pair<String, AccessDownload>? {
        val id = calcID()
        val file = File(path)
        if (!file.exists()) {
            soundError()
            sendMessage(prefix + cmp("The file $path does not exist!", cError))
            sendMessage(prefix + cmp("Note, the path starts in your server directory (or of the start script)"))
            return null
        }
        val zipTarget = if (file.isDirectory) {
            val zipFile = calcTempZip(file.nameWithoutExtension, WebServer.tempFolder)
            Zipping.zipFolder(file, zipFile)
            zipFile.deleteOnExit() // Cache clean up
            zipFile.path
        } else null
        val timeoutTimestamp = if (duration != null) {
            System.currentTimeMillis() + duration.inWholeMilliseconds
        } else null

        val data = AccessDownload(file.path, zipTarget, access, restriction, timeoutTimestamp, maxDownloads)
        ServerData.addWhitelist(id, data)

        sendMessage(prefix + cmp("New file access created!", cSuccess))
        printLink(data, id)
        return id to data
    }

    fun Audience.whitelistUpload(path: String, access: WhitelistType, restriction: String? = null, maxFileSize: Long? = null, maxFileAmount: Int = 1, duration: Duration? = null): Pair<String, AccessUpload>? {
        val id = calcID()
        val file = File(path)
        if (!file.isDirectory) {
            soundError()
            sendMessage(prefix + cmp("The file $path is not a directory!", cError))
            return null
        }

        val timeoutTimestamp = if (duration != null) {
            System.currentTimeMillis() + duration.inWholeMilliseconds
        } else null

        val data = AccessUpload(file.path, access, restriction, maxFileSize, maxFileAmount, timeoutTimestamp)
        ServerData.addUpload(id, data)

        sendMessage(prefix + cmp("New upload folder access created!", cSuccess))
        printLink(data, id)
        return id to data
    }

    fun Audience.removeWhitelist(id: String): Boolean {
        return if (ServerData.removeWhitelist(id)) {
            soundDisable()
            sendMessage(prefix + cmp("Successfully removed file access!", cSuccess))
            true
        } else {
            soundError()
            sendMessage(prefix + cmp("Failed to remove file access!", cError))
            false
        }
    }

    fun Audience.removeUpload(id: String): Boolean {
        return if (ServerData.removeUpload(id)) {
            soundDisable()
            sendMessage(prefix + cmp("Successfully removed upload access", cSuccess))
            true
        } else {
            soundError()
            sendMessage(prefix + cmp("Failed to remove upload access", cError))
            false
        }
    }

    fun Audience.printLink(fileData: AccessData, id: String) {
        soundEnable()
        if (this == consoleAudience) {
            sendMessage(cmp("The access ID is ") + cmp(id, cMark))
        } else {
            sendMessage(
                prefix + cmp("Click ") +
                        cmp("here", cMark)
                            .clickEvent(ClickEvent.copyToClipboard(ServerData.getLink(id, fileData is AccessDownload)))
                            .addHover(cmp("Click to copy access link")) +
                        cmp(" to copy the access link ${if (fileData.accessType == WhitelistType.PASSPHRASE_RESTRICTED) "(passphrase included!)" else ""}")
            )
        }
    }

    fun Audience.createResourcePackAccess(path: String): ResourcePackInfo? {
        val whitelist = Audience.empty().whitelistFile(path, WhitelistType.GLOBAL)
        if (whitelist == null) {
            sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(path)), cError))
            return null
        }

        val file = File(whitelist.second.zippedTo ?: whitelist.second.path)
        val hash = DigestUtils.getSha1Digest().digest(file.readBytes())

        val prompt = msg("event.texturepackPrompt", listOf(file.name))
        val link = ServerData.getLink(whitelist.first, true) + "?direct=true"
        return ResourcePackInfo(hash, prompt, link, whitelist.first)
    }

    private fun calcID(): String {
        val id = System.currentTimeMillis().toString()
        return buildString {
            id.forEach { digit ->
                append(if (Random.nextBoolean()) charList[digit.digitToInt()] else digit)
            }
        }
    }

    private fun calcTempZip(currentName: String, tempFolder: File): File {
        val possibleFile = File(tempFolder, "$currentName.zip")
        return if (possibleFile.exists()) calcTempZip(currentName + Random.nextInt(0..9), tempFolder)
        else possibleFile
    }

    @Suppress("ArrayInDataClass")
    data class ResourcePackInfo(val hash: ByteArray, val prompt: Component, val link: String, val data: String)
}