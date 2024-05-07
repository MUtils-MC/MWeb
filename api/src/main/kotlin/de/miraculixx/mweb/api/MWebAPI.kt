package de.miraculixx.mweb.api

import de.miraculixx.mweb.api.data.AccessDownload
import de.miraculixx.mweb.api.data.WhitelistType
import java.io.File
import java.net.URL
import java.util.*
import kotlin.time.Duration

abstract class MWebAPI {
    companion object {
        var INSTANCE: MWebAPI? = null
    }

    /**
     * Create a new file whitelist. All needed actions proceed automatically
     * @param path Path to the file
     * @param access Who can access the file
     * @param restriction Restriction string for restricted access
     * @param duration Whitelist timeout
     * @param maxDownloads Download amount restriction
     * @return Pair out ID & [AccessDownload]
     */
    abstract fun whitelistFile(path: String, access: WhitelistType, restriction: String? = null, duration: Duration? = null, maxDownloads: Int? = null): Pair<String, AccessDownload>?

    /**
     * Remove an existing whitelist for a file
     * @param id Whitelist ID
     * @return True if the whitelist existed
     */
    abstract fun removeWhitelist(id: String): Boolean

    /**
     * Send a folder or archive directly as resource pack to given targets
     * @param path Target folder or archive
     * @param targets All receiving targets
     * @return false if the file does not exist or is not a folder or archive
     */
    abstract fun sendFileAsResourcePack(path: String, targets: Set<UUID>, force: Boolean): Boolean

    /**
     * Sends multiple folders or archives directly as multiple resource packs to given targets
     * @param paths Target folders or archives
     * @param targets All receiving targets
     * @return false if the file does not exist or is not a folder or archive
     */
    abstract fun sendFilesAsResourcePacks(paths: Set<String>, targets: Set<UUID>, force: Boolean): Boolean

    /**
     * Zip a folder to a given destination
     * @param folder Source folder
     * @param target Target zip (must not exist)
     * @return true if successfully
     */
    abstract fun zipFolder(folder: File, target: File): Boolean

    /**
     * Unzip a folder to a given destination
     * @param zip Source archive
     * @param target Target folder (must not exist)
     * @return true if successfully
     */
    abstract fun unZipFolder(zip: File, target: File): Boolean
}