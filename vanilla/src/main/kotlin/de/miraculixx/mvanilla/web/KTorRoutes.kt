package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.serializer.Zipping
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.File
import java.nio.file.Files
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun Application.configureDownloads() {
    val respondForbidden = File(configFolder, "responses/forbidden.html").takeIf { it.isFile }?.readText() ?: "Forbidden Access"
    val respondNoID = File(configFolder, "responses/invalid.html").takeIf { it.isFile }?.readText() ?: "Invalid ID"
    val respondNotFound = File(configFolder, "responses/notfound.html").takeIf { it.isFile }?.readText() ?: "Access not found"
    val respondIndex = File(configFolder, "responses/index.html").takeIf { it.isFile }?.readText() ?: "Welcome to MWeb"
    val respondFileInfo = File(configFolder, "responses/download.html").takeIf { it.isFile }?.readText() ?: "Download prompt is not configured"

    suspend fun PipelineContext<Unit, ApplicationCall>.handleDownloadRequest() {
        val id = call.parameters["id"]
        val passphrase = call.parameters["pw"]
        val direct = call.parameters["direct"]
        if (id == null) {
            call.respondText(respondNoID, ContentType.Text.Html, HttpStatusCode.BadRequest)
            return
        }

        // Check if file exist and is not timed out
        val fileData = ServerData.getFileData(id)
        if (fileData == null || ServerData.isUnavailable(fileData)) {
            call.respondText(respondNotFound, ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        }

        // Check if user has access to download
        val requestIP = call.request.origin.remoteHost
        if (!ServerData.hasAccess(requestIP, id, passphrase)) {
            call.respondText(respondForbidden, ContentType.Text.Html, HttpStatusCode.Forbidden)
            return
        }

        // Return final file. If zip target is provided a cache check is performed
        val playerID = ServerData.ipToPlayer(requestIP)
        val playerName = playerID?.let { uuid -> LoaderSpecific.INSTANCE?.uuidToPlayerName(uuid) ?: uuid } ?: requestIP

        val realFileTarget = File(fileData.path)
        val finalFile = if (fileData.zippedTo != null) {
            val zipFile = File(fileData.zippedTo)
            if (!zipFile.exists()) {
                if (settings.logAccess) consoleAudience.sendMessage(prefix + cmp("REQUEST Zip target $id to ${zipFile.path}"))
                Zipping.zipFolder(realFileTarget, zipFile)
                zipFile.deleteOnExit() // Clean up temp cache
            }
            zipFile
        } else if (realFileTarget.isDirectory || !realFileTarget.exists()) {
            call.respondText(respondNotFound, ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        } else realFileTarget

        // Check if file respond or prompt
        if (direct != "true") {
            call.respondText(respondFileInfo
                .replace("\${fileName}", finalFile.name)
                .replace("\${fileSize}", humanReadableByteCountSI(Files.size(finalFile.toPath())))
                .replace("\${fileDate}", FileType.getTime(Instant.ofEpochMilli(finalFile.lastModified()))),
            ContentType.Text.Html, HttpStatusCode.OK)
            return
        }

        if (settings.logAccess) consoleAudience.sendMessage(prefix + cmp("REQUEST $playerName: $id"))
        call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"${finalFile.name}\"")
        call.respondFile(finalFile)

        // Update link requests
        fileData.requestAmount += 1
        fileData.maxAmount?.let { if (it >= fileData.requestAmount) fileData.disabled = true }
        fileData.timeout?.let { if (System.currentTimeMillis() >= it) fileData.disabled = true }
    }

    routing {
        get("/d/{id}") { handleDownloadRequest() }
        get("/d") { handleDownloadRequest() }
        get("/download/{id}") { handleDownloadRequest() }
        get("/download") { handleDownloadRequest() }
        get{ call.respondText(respondIndex, ContentType.Text.Html, HttpStatusCode.OK) }
    }
}

fun humanReadableByteCountSI(bytes: Long): String {
    var bytes = bytes
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current())
}