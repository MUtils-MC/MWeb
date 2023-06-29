package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mweb.api.data.AccessUpload
import de.miraculixx.mweb.api.utils.humanReadableByteCountSI
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.File
import java.nio.file.Files
import java.time.Instant


fun Application.configureDownloads() {
    val respondForbidden = File(configFolder, "responses/forbidden.html").takeIf { it.isFile }?.readText() ?: "Forbidden Access"
    val respondNoID = File(configFolder, "responses/invalid.html").takeIf { it.isFile }?.readText() ?: "Invalid ID"
    val respondNotFound = File(configFolder, "responses/notfound.html").takeIf { it.isFile }?.readText() ?: "Access not found"
    val respondIndex = File(configFolder, "responses/index.html").takeIf { it.isFile }?.readText() ?: "Welcome to MWeb"
    val respondFileInfo = File(configFolder, "responses/download.html").takeIf { it.isFile }?.readText() ?: "Download prompt is not configured"
    val respondUploadInfo = File(configFolder, "responses/upload.html").takeIf { it.isFile }?.readText() ?: "Upload prompt is not configured"
    val respondUploadSuccess = File(configFolder, "responses/uploaded.html").takeIf { it.isFile }?.readText() ?: "Successfully uploaded"

    suspend fun PipelineContext<Unit, ApplicationCall>.handleDownloadRequest() {
        val id = call.parameters["id"]
        val passphrase = call.parameters["pw"]
        val direct = call.parameters["direct"]
        if (id == null) {
            call.respondText(respondNoID, ContentType.Text.Html, HttpStatusCode.BadRequest)
            return
        }

        // Check if the file exists and is not timed out
        val fileData = ServerData.getFileData(id)
        if (fileData == null || ServerData.isFileUnavailable(fileData)) {
            call.respondText(respondNotFound, ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        }

        // Check if user has access to download
        val requestIP = call.request.origin.remoteHost
        if (!ServerData.fileHasAccess(requestIP, id, passphrase)) {
            call.respondText(respondForbidden, ContentType.Text.Html, HttpStatusCode.Forbidden)
            return
        }

        // Return final file. If a zip target is provided, a cache check is performed
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

        // Check if file responds or prompts
        if (direct != "true") {
            call.respondText(
                respondFileInfo
                    .replace("\${fileName}", finalFile.name)
                    .replace("\${fileSize}", humanReadableByteCountSI(Files.size(finalFile.toPath())))
                    .replace("\${fileDate}", FileType.getTime(Instant.ofEpochMilli(finalFile.lastModified()))),
                ContentType.Text.Html, HttpStatusCode.OK
            )
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

    suspend fun PipelineContext<Unit, ApplicationCall>.sendUploadSuccess(size: String, name: String, amount: Int, error: Boolean, uploadData: AccessUpload) {
        call.respondText(
            respondUploadSuccess
                .replace("\${fileName}", name.removeSuffix(", "))
                .replace("\${fileSize}", size),
            ContentType.Text.Html, HttpStatusCode.OK
        )
        uploadData.uploadedFiles += amount
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.handleUploadRequest(post: Boolean) {
        val id = call.parameters["id"]
        val passphrase = call.parameters["pw"]
        if (id == null) {
            call.respondText(respondNoID, ContentType.Text.Html, HttpStatusCode.BadRequest)
            return
        }

        // Check if the file exists and is not timed out
        val uploadData = ServerData.getUploadData(id)
        if (uploadData == null || ServerData.isUploadUnavailable(uploadData)) {
            call.respondText(respondNotFound, ContentType.Text.Html, HttpStatusCode.NotFound)
            return
        }

        // Check if user has access to download
        val requestIP = call.request.origin.remoteHost
        if (!ServerData.uploadHasAccess(requestIP, id, passphrase)) {
            call.respondText(respondForbidden, ContentType.Text.Html, HttpStatusCode.Forbidden)
            return
        }

        if (!post) {
            call.respondText(
                respondUploadInfo
                    .replace("\${fileSize}", uploadData.maxFileSize?.let { humanReadableByteCountSI(it) } ?: "Unlimited")
                    .replace("\${fileAmount}", uploadData.maxFileAmount.toString()),
                ContentType.Text.Html, HttpStatusCode.OK
            )
        } else { //Receive Upload

            val multipartData = call.receiveMultipart()
            var fileDescription = ""
            val totalSize = call.request.header(HttpHeaders.ContentLength) ?: "Unknown"
            var amount = 0
            var error = false
            val maxAmount = uploadData.maxFileAmount
            val maxSize = uploadData.maxFileSize ?: Long.MAX_VALUE
            if (totalSize.toLong() > maxAmount.toLong() * maxSize) {
                sendUploadSuccess(totalSize, "File/s too big!", 0, true, uploadData)
                return
            }

            multipartData.forEachPart { part ->
                if (error) {
                    part.dispose
                    return@forEachPart
                }
                when (part) {
                    is PartData.FileItem -> {
                        if (amount + 1 > maxAmount) {
                            error = true
                            return@forEachPart
                        }
                        val fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        if (fileBytes.size.toLong() > maxSize) {
                            error = true
                            return@forEachPart
                        }
                        fileDescription += "$fileName, "
                        File(uploadData.path, fileName).writeBytes(fileBytes)
                        amount++
                    }

                    else -> Unit
                }
                part.dispose()
            }
            sendUploadSuccess(totalSize, fileDescription, amount, error, uploadData)
        }
    }

    routing {
        get("/d/{id}") { handleDownloadRequest() }
        get("/d") { handleDownloadRequest() }
        get("/download/{id}") { handleDownloadRequest() }
        get("/download") { handleDownloadRequest() }
        get("u/{id}") { handleUploadRequest(false) }
        get("upload/{id}") { handleUploadRequest(false) }
        post("u/{id}") { handleUploadRequest(true) }
        post("upload/{id}") { handleUploadRequest(true) }
        get { call.respondText(respondIndex, ContentType.Text.Html, HttpStatusCode.OK) }
    }
}