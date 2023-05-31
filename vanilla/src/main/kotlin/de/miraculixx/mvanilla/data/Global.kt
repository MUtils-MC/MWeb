package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.messages.cHighlight
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.io.File

lateinit var configFolder: File
lateinit var settings: Settings
lateinit var consoleAudience: Audience
val prefix = cmp("MWeb", cHighlight) + cmp(" >>", NamedTextColor.DARK_GRAY) + cmp(" ")
val miniMessage = MiniMessage.miniMessage()
val plainSerializer = PlainTextComponentSerializer.plainText()