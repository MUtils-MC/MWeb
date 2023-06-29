package de.miraculixx.mweb.api.data

import net.kyori.adventure.text.Component

interface AccessData {
    val path: String
    val accessType: WhitelistType
    val restriction: String?
    var disabled: Boolean
    var timeout: Long?

    fun compactLore(spacer: Component): Component
    fun fullLore(spacer: Component): List<Component>
}