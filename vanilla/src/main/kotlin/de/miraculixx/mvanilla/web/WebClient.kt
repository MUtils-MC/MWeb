package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import de.miraculixx.mweb.api.data.LogPayload
import de.miraculixx.mweb.api.data.LogPayloadData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object WebClient {
    private var ktor: HttpClient? = null
    private val json = Json {
        encodeDefaults = true
        prettyPrint = false
    }

    val logbacks = mutableMapOf<String, LogPayload>()

    suspend fun sendMultipart(url: String, files: Set<File>, data: LogPayloadData): Response {
        return try {
            if (ktor == null) ktor = HttpClient(CIO)
            val code = ktor?.post(url) {
                setBody(
                    MultiPartFormDataContent(formData {
                        append("data", json.encodeToString(data))

                        files.forEachIndexed { index, file ->
                            append("files[$index]", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                            })
                        }
                    })
                )
            }?.status ?: return Response.INTERNAL_ERROR
            consoleAudience.sendMessage(prefix + cmp("Webhook endpoint responded with code $code"))
            when {
                code.isSuccess() -> Response.SUCCESS
                code == HttpStatusCode.Forbidden -> Response.INVALID_CODE //403
                else -> Response.API_ERROR
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Response.INTERNAL_ERROR
        }
    }

    enum class Response {
        SUCCESS, INVALID_CODE, API_ERROR, INTERNAL_ERROR
    }
}