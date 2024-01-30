package de.miraculixx.mweb.api.data

import kotlinx.serialization.Serializable
import java.io.File

data class LogPayload(
    val files: Set<File>,
    val cooldown: Int,
    val webhook: String,
    val zip: Boolean,
    val data: LogPayloadData
)

@Serializable
data class LogPayloadData(
    val code: String,
    val timestamp: Long,
    val mWebVersion: String,
    val mod: LogPayloadModData,
    val server: LogPayloadServerData
)

@Serializable
data class LogPayloadModData(
    val id: String,
    val version: String
)

@Serializable
data class LogPayloadServerData(
    val version: String,
    val loader: String,
    val system: String
)

/**
 * {
 *   "code": "abc",
 *   "timestamp": "12345",
 *   "mweb-version": "1.0.0",
 *   "mod": {
 *     "id": "mweb",
 *     "version": "1.0.0"
 *   },
 *   "server": {
 *     "version": "1.20.4",
 *     "loader": "paper-13.42.1",
 *     "system": "linux"
 *   }
 * }
 */