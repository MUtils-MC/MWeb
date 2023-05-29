package de.miraculixx.webserver.web

import de.miraculixx.webserver.utils.*
import de.miraculixx.webserver.utils.messages.cmp
import de.miraculixx.webserver.utils.messages.plus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

fun Application.configureDownloads() {
    routing {
        get("/download") {
            val path = call.parameters["path"]
            val shouldZip = call.parameters["zip"] == "true"
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            // Check if file is whitelisted
            if (!ServerData.getWhitelistedFiles().contains(path)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            // Check if file exist and is no folder (or zipping is active)
            val targetFile = File(path)
            if (!targetFile.exists() || (targetFile.isDirectory && !shouldZip)) {
                call.respond(HttpStatusCode.NotFound, "Invalid Path")
                return@get
            }

            val requestIP = call.request.origin.remoteHost

            // Return final file and zip if file is folder
            val finalFile = if (targetFile.isDirectory) {
                if (!WebServer.tempFolder.exists()) WebServer.tempFolder.mkdir()
                val zipFile = File(WebServer.tempFolder, "${UUID.randomUUID()}.zip")
                Zipping.zipFolder(targetFile, zipFile)
                zipFile.deleteOnExit()
                if (settings.logAccess) consoleAudience.sendMessage(prefix + cmp("REQUEST ${ServerData.ipToPlayer(requestIP) ?: requestIP}: $path (zipped to $zipFile)"))
                zipFile
            } else {
                if (settings.logAccess) consoleAudience.sendMessage(prefix + cmp("REQUEST ${ServerData.ipToPlayer(requestIP) ?: requestIP}: $path"))
                targetFile
            }
            call.respondFile(finalFile)
        }
    }
}