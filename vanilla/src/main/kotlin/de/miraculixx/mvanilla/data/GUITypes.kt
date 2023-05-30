package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.messages.cHighlight
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

enum class GUITypes(val title: Component) {
    FILE_MANAGE(cmp("• ", NamedTextColor.DARK_GRAY) + cmp("File Manager", cHighlight)),
    FILE_WHITELISTING(cmp("• ", NamedTextColor.DARK_GRAY) + cmp("File Whitelisting", cHighlight)),
    FILE_UPLOADING(cmp("• ", NamedTextColor.DARK_GRAY) + cmp("File Uploading", cHighlight)),
}