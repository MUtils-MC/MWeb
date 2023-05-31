package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import java.time.Instant

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
    var maxAmount: Int? = null,
    var requestAmount: Int = 0,
    var disabled: Boolean = false
) {
    fun fullLore(spacer: Component): List<Component> {
        return buildList {
            add(spacer + cmp(accessType.name))
            restriction?.let { add(spacer + cmp("Restriction: $it")) }
            timeout?.let { add(spacer + cmp("Timeout: ${FileType.getTime(Instant.ofEpochMilli(it))}")) }
            add(spacer + cmp("Requested: $requestAmount${maxAmount?.let { "/$it" } ?: ""}"))
            zippedTo?.let { add(spacer + cmp("Cached: $it")) }
        }
    }

    fun compactLore(spacer: Component): Component {
        return spacer + cmp("$accessType ($requestAmount${maxAmount?.let { "/$it" } ?: ""})")
    }
}