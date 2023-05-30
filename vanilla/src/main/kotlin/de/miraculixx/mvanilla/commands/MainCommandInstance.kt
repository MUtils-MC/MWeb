package de.miraculixx.mvanilla.commands

import de.miraculixx.mvanilla.data.WhitelistType
import kotlin.time.Duration

interface MainCommandInstance {
    /**
     * Whitelist a file to create a download link
     * @param path Path to the file
     * @param access Access type - Restrict file to certain users
     * @param restriction Needed if access type is restricted. UUID, permission, ...
     * @param duration Access only for a limited duration. Null = infinity
     */
    fun whitelistFile(path: String, access: WhitelistType, restriction: String? = null, duration: Duration? = null) {

    }
}