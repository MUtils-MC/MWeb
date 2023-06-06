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

fun Application.configureDownloads() {
    val respondForbidden = File(configFolder, "responses/forbidden.html")
    val respondNoID = File(configFolder, "responses/invalid.html")
    val respondNotFound = File(configFolder, "responses/notfound.html")

    suspend fun PipelineContext<Unit, ApplicationCall>.handleDownloadRequest() {
        val id = call.parameters["id"]
        val passphrase = call.parameters["pw"]
        if (id == null) {
            call.respondText(respondNoID.takeIf { it.isFile }?.readText() ?: "Invalid ID", ContentType.Text.Html, HttpStatusCode.BadRequest)
            return
        }

        // Check if file exist and is not timed out
        val fileData = ServerData.getFileData(id)
        if (fileData == null || ServerData.isUnavailable(fileData)) {
            call.respondText(respondNotFound.takeIf { it.isFile }?.readText() ?: "Access not found", ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        }

        // Check if user has access to download
        val requestIP = call.request.origin.remoteHost
        if (!ServerData.hasAccess(requestIP, id, passphrase)) {
            call.respondText(respondForbidden.takeIf { it.isFile }?.readText() ?: "Forbidden Access", ContentType.Text.Html, HttpStatusCode.Forbidden)
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
            call.respondText(respondNotFound.takeIf { it.isFile }?.readText() ?: "Access not found", ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        } else realFileTarget
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
    }
}