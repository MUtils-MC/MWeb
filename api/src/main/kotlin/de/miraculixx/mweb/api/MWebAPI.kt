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


    //
    // AUTO LOG API
    //

    /**
     * Define a new log endpoint for your mod to let users send all important logs/configs to you without explaining them what you need and how to get it.
     *
     * If registered, you can request the log from the user by giving them a unique code (explained below).
     * The user simply needs to enter **"/msend <mod-id> <code>"** in the chat or console (user will still be prompted that provided files will be sent).<br>
     *
     * ## Webhook & code usage
     * Webhooks are any URLs that can receive POST requests.
     * Please note, your webhook URL will be visible to users if they use certain tools to extract the code.
     * Therefore, we send the provided code as an url parameter and in the json body to verify the request (details at the end).
     *
     * ### 1. Discord Bot
     * If you want your logs/config to be sent to a discord channel, you can use our [bot](https://mutils.net/mweb/dcbot) to handle all requests.
     * With the bot you can enter **"/create [<channel/thread>]"** to create a new request code and give it the user.
     *
     * Providing no target channel will use the channel the command was executed in.
     *
     * Every code is only valid for the defined time in the bot settings and can only be used once.
     * Users can only send logs/configs each [cooldown] seconds to avoid spamming.
     *
     * **Webhook** -> https://api.mweb.mutils.net/webhook/<mod-id>/<guild-id>
     *
     * ### 2. Custom backend
     * If you want to handle the requests yourself, you can use any backend that can receive POST requests (e.g. [Ktor](https://ktor.io/docs/intellij-idea.html)).
     *
     * Users can only send logs/configs each [cooldown] seconds to avoid spamming.
     *
     * The [timeout] value will be ignored and the **timestamp** value must be used to verify the request timeout.
     *
     * MWeb sends a multipart body with the following structure:
     * - **payload_json** - The json body with the following structure: {"code": "...", "timestamp": "<unix-timestamp-seconds>", "mod-id": "..."}
     * - **file[[n]]** - All files provided by [files] if present.
     *   n is the index of the file starting at 0. If zipping is enabled, there will only be one file.
     *
     * The code will also be sent as an url parameter with the following formatting:
     * - https://your-webhook-url?code=...
     *
     *
     * @param modInstance The main class of your mod/plugin. For paper, the class must extend [JavaPlugin] or similar. For fabric, the class must implement [ModInitializer].
     * @param modID The unique ID/name of your mod/plugin. This will be used to identify the mod/plugin in requests and will be prompted to the user.
     * @param webhookURL The URL that will receive the POST requests. See [registerLogSending] for more details.
     * @param files All files that should be sent to the webhook. If zipping is enabled, only one file will be sent. Files are restricted to the following locations for security: /logs, /plugins, /config, /mods
     * @param cooldown The cooldown in seconds between two requests. If the user tries to send a log/config before the cooldown is over, they will be prompted with the remaining time.
     * @param zip If true, all files will be zipped before sending. This will reduce the number of files sent to the webhook to one.
     * @see unregisterLogSending
     */
    abstract fun registerLogSending(modInstance: Any, modID: String, webhookURL: URL, files: Set<File>, cooldown: Int, zip: Boolean = false): Boolean

    /**
     * Unregister a log endpoint created with [registerLogSending]. This will stop any requests from being sent to the webhook.
     * @param modInstance The main class of your mod/plugin. For paper, the class must extend [JavaPlugin] or similar. For fabric, the class must implement [ModInitializer].
     * @param modID The unique ID/name of your mod/plugin. This will be used to identify the mod/plugin in requests and will be prompted to the user.
     * @see registerLogSending
     */
    abstract fun unregisterLogSending(modInstance: Any, modID: String): Boolean
}