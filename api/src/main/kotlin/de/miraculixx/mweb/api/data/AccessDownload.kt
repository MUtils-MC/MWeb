package de.miraculixx.mweb.api.data

import de.miraculixx.mweb.api.utils.getTime
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
data class AccessDownload(
    override val path: String,
    val zippedTo: String? = null,
    override val accessType: WhitelistType,
    override val restriction: String? = null,
    override var timeout: Long? = null,
    var maxAmount: Int? = null,
    var requestAmount: Int = 0,
    override var disabled: Boolean = false
): AccessData {
    override fun fullLore(spacer: Component): List<Component> {
        return buildList {
            add(spacer.append(Component.text(accessType.name)))
            restriction?.let { add(spacer.append(Component.text("Restriction: $it"))) }
            timeout?.let { add(spacer.append(Component.text("Timeout: ${getTime(Instant.ofEpochMilli(it))}"))) }
            add(spacer.append(Component.text("Requested: $requestAmount${maxAmount?.let { "/$it" } ?: ""}")))
            zippedTo?.let { add(spacer.append(Component.text("Cached: $it"))) }
        }
    }

    override fun compactLore(spacer: Component): Component {
        return spacer.append(Component.text("$accessType ($requestAmount${maxAmount?.let { "/$it" } ?: ""})"))
    }
}