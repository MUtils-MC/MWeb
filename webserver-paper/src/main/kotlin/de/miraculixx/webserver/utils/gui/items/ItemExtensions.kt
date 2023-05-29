package de.miraculixx.webserver.utils.gui.items

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import de.miraculixx.webserver.utils.consoleAudience
import de.miraculixx.webserver.utils.messages.cError
import de.miraculixx.webserver.utils.messages.cmp
import de.miraculixx.webserver.utils.messages.plus
import de.miraculixx.webserver.utils.prefix
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*

fun SkullMeta.skullTexture(base64: String, uuid: UUID = UUID.randomUUID()): SkullMeta {
    val profile = GameProfile(uuid, "")
    profile.properties.put("textures", Property("textures", base64))
    val profileField: Field?
    try {
        profileField = javaClass.getDeclaredField("profile")
        profileField.isAccessible = true
        profileField[this] = profile
    } catch (e: Exception) {
        e.printStackTrace()
        consoleAudience.sendMessage(prefix + cmp("Head Builder failed to apply Base64 Code to Skull!", cError))
        consoleAudience.sendMessage(prefix + cmp("Code: ", cError) + cmp(base64))
    }
    return this
}