package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.serializer.Zipping
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureDownloads() {
    routing {
        get("/download/{id}") {
            val id = call.parameters["id"]
            val passphrase = call.parameters["pw"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            // Check if file exist and is not timed out
            val fileData = ServerData.getFileData(id)
            val currentTime = System.currentTimeMillis()
            if (fileData == null || (fileData.timeout != null && fileData.timeout!! >= currentTime)) {
                call.respond(HttpStatusCode.NotFound, "Invalid Path")
                return@get
            }

            // Check if user has access to download
            val requestIP = call.request.origin.remoteHost
            if (!ServerData.hasAccess(requestIP, id, passphrase)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
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
                call.respond(HttpStatusCode.BadRequest, "Target file got moved")
                return@get
            } else realFileTarget
            if (settings.logAccess) consoleAudience.sendMessage(prefix + cmp("REQUEST $playerName: $id"))

            call.respondFile(finalFile)
        }
    }
}