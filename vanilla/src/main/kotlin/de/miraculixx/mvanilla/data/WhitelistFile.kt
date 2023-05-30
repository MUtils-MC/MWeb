package de.miraculixx.mvanilla.data

import kotlinx.serialization.Serializable

/**
 * @param path Real path to file
 * @param zippedTo Path to temporary cached zip file
 * @param accessType Access type
 * @param restriction Restriction string if type is restricted. Could be IP, UUID, passphrase, ...
 * @param timeout The epoch timestamp (in millis) when the file access is revoked
 */
@Serializable
data class WhitelistFile(
    val path: String,
    val zippedTo: String? = null,
    val accessType: WhitelistType,
    val restriction: String? = null,
    var timeout: Long? = null,
)