package de.miraculixx.mweb.api.data

import de.miraculixx.mweb.api.utils.getTime
import de.miraculixx.mweb.api.utils.humanReadableByteCountSI
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import java.time.Instant

/**
 * @param path Real path to file
 * @param maxFileAmount Maximal files that are allowed to upload simultaneously
 * @param maxFileSize Maximal size of a single file in bytes
 * @param accessType Access type
 * @param restriction Restriction string if type is restricted. Could be IP, UUID, passphrase, ...
 * @param timeout The epoch timestamp (in millis) when the file access is revoked
 */
@Serializable
data class AccessUpload(
    override val path: String,
    override val accessType: WhitelistType,
    override val restriction: String? = null,
    val maxFileSize: Long? = null,
    val maxFileAmount: Int = 1,
    override var timeout: Long? = null,
    var uploadedFiles: Int = 0,
    override var disabled: Boolean = false,
): AccessData {
    override fun fullLore(spacer: Component): List<Component> {
        return buildList {
            add(spacer.append(Component.text(accessType.name)))
            restriction?.let { add(spacer.append(Component.text("Restriction: $it"))) }
            timeout?.let { add(spacer.append(Component.text("Timeout: ${getTime(Instant.ofEpochMilli(it))}"))) }
            add(spacer.append(Component.text("Max File Size: $maxFileSize")))
            add(spacer.append(Component.text("Max File Amount: $maxFileAmount")))
            add(spacer.append(Component.text("Uploads: $uploadedFiles")))
        }
    }

    override fun compactLore(spacer: Component): Component {
        return spacer.append(Component.text("$accessType (max ${maxFileSize?.let { humanReadableByteCountSI(it) } ?: "∞"} - *$maxFileAmount)"))
    }
}