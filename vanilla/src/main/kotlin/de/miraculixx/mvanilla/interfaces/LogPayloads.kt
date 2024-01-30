package de.miraculixx.mvanilla.interfaces

import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mvanilla.serializer.regexIP
import de.miraculixx.mvanilla.web.WebClient
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.api.data.LogPayloadData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

interface LogPayloads {
    val allowedFolders: Set<String>
        get() = setOf("logs", "plugins", "config", "mods")
    val idChars: String
        get() = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    /**
     * Redact IP addresses from a file and return a new file with the redacted content.
     */
    fun redactIPAddresses(source: File): File {
        consoleAudience.sendMessage(prefix + cmp("Redacting IP addresses from ${source.path}..."))
        val redactedTempFile = File(WebServer.tempFolder, "${UUID.randomUUID()}.${source.extension.ifBlank { "temp" }}")
        redactedTempFile.bufferedWriter().use { writer ->
            source.forEachLine { line ->
                writer.write(line.replace(regexIP, "<ip-address>") + "\n")
            }
        }
        return redactedTempFile.apply { deleteOnExit() }
    }

    fun prepareFiles(targets: Set<File>, zip: Boolean): Set<File> {
        consoleAudience.sendMessage(prefix + cmp("Preparing ${targets.size} (zip $zip) files to sent..."))
        val finalFiles = targets.mapNotNull { file ->
            val extension = file.extension
            when {
                // Receive the latest crash report if crash report folder is requested
                file.name == "crash-reports" -> {
                    file.listFiles()?.filter { it.isFile && extension == "txt" }
                        ?.maxByOrNull { it.lastModified() }
                        ?.let { redactIPAddresses(it) }
                }

                // Check if the file is in an allowed directory
                isInAllowedDirectory(file.path) -> null

                // Zip the file if it is a directory
                file.isDirectory -> {
                    consoleAudience.sendMessage(prefix + cmp("Zipping folder ${file.path}..."))
                    val tempZipFile = File(WebServer.tempFolder, "${file.name}.${getRandomID()}.zip")
                    Zipping.zipFolder(file, tempZipFile)
                    tempZipFile.apply { deleteOnExit() }
                }

                // Redact IP addresses from log files
                file.extension == "log" || file.extension == "txt" -> redactIPAddresses(file)

                else -> file
            }
        }.toMutableList()

        if (zip) {
            consoleAudience.sendMessage(prefix + cmp("Zipping all ${finalFiles.size} files together..."))
            val tempZipFile = File(WebServer.tempFolder, "${UUID.randomUUID()}.zip").apply { deleteOnExit() }
            Zipping.zipFiles(finalFiles, tempZipFile)
            finalFiles.clear()
            finalFiles.add(tempZipFile)
        }
        return finalFiles.toSet()
    }

    suspend fun sendPayload(targets: Set<File>, webhook: String, payloadData: LogPayloadData): WebClient.Response {
        consoleAudience.sendMessage(prefix + cmp("Sending ${targets.size} files to $webhook..."))
        return WebClient.sendMultipart(webhook, targets, payloadData)
    }

    fun Audience.commandResponseInfo(type: String) {
        sendMessage(prefix + cmp("Easily send important logs and $type configurations to their developers to speed up the debugging and support process. " +
                "You will receive a code from the developer which you can enter here to securely send all specified files. " +
                "Before sending, you can review the files and cancel the request at any time."))
        sendMessage(prefix + cmp("Usage: ") + cmp("/mlogs <$type> <code>", cMark))
    }

    fun Audience.commandResponseMod(mod: String, type: String) {
        val data = WebClient.logbacks[mod]
        if (data == null) {
            noSupport(mod)
            return
        }
        sendMessage(prefix + cmp("The $type ") + cmp(mod, cMark) + cmp(" requests following files to be sent to their developers on support:"))
        printFiles(data.files)
    }

    fun Audience.commandResponseCode(plugin: String, code: String, cooldown: MutableSet<String>, confirmations: MutableMap<Audience, String>) {
        val data = WebClient.logbacks[plugin]
        if (data == null) {
            noSupport(plugin)
            return
        }

        if (confirmations.containsKey(this)) {
            sendMessage(prefix + cmp("Sending files to the developer..."))
            CoroutineScope(Dispatchers.Default).launch {
                val finalFiles = prepareFiles(data.files, data.zip)
                when (sendPayload(finalFiles, "${data.webhook}?code=$code", data.data)) {
                    WebClient.Response.SUCCESS -> {
                        cooldown.add(plugin)
                        sendMessage(prefix + cmp("Files sent successfully!", cSuccess))
                        launch {
                            delay(data.cooldown.seconds)
                            cooldown.remove(plugin)
                        }
                    }
                    WebClient.Response.INVALID_CODE -> sendMessage(prefix + cmp("The code you entered is invalid for $plugin!", cError))
                    WebClient.Response.INTERNAL_ERROR -> sendMessage(prefix + cmp("An error occurred while sending the files! Please check the console for more information", cError))
                    WebClient.Response.API_ERROR -> sendMessage(prefix + cmp("The endpoint responded with an error. Please notify the developers about this behavior!", cError))
                }
            }
        } else {
            sendMessage(prefix + cmp("You are about to send the following files to the developer of ") + cmp(plugin, cMark) + cmp(":"))
            printFiles(data.files)
            sendMessage(prefix + cmp("Confirm by entering the command again!", cSuccess))
            confirmations[this] = plugin
        }
    }


    //
    // Internal functions
    //

    private fun isInAllowedDirectory(path: String): Boolean {
        val firstFolder = path.substringBefore(File.pathSeparator)
        return firstFolder != path && firstFolder in allowedFolders
    }

    private fun getRandomID(length: Int = 5) = buildString { repeat(length) { append(idChars.random()) } }

    private fun Audience.printFiles(files: Set<File>) {
        files.forEach { sendMessage(cmp("· ", cHighlight) + cmp(it.path)) }
    }

    private fun Audience.noSupport(plugin: String) {
        sendMessage(prefix + cmp("The plugin ", cError) + cmp(plugin, cMark) + cmp(" does not support easy support yet!", cError))
        sendMessage(prefix + cmp("Contact the developer of the plugin and ask them to add support for MLogs.", cError))
    }
}