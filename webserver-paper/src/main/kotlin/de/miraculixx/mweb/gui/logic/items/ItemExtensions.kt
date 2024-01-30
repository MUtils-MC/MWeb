package de.miraculixx.mweb.gui.logic.items

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

fun SkullMeta.skullTexture(base64: String, uuid: UUID = UUID.randomUUID()): SkullMeta {
    playerProfile = Bukkit.createProfile(uuid).apply { setProperty(ProfileProperty("textures", base64)) }
    return this
}