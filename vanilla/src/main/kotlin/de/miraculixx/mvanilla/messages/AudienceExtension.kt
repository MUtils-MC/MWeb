package de.miraculixx.mvanilla.messages

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun Audience.title(main: Component, sub: Component, fadeIn: Duration = Duration.ZERO, stay: Duration = 5.seconds, fadeOut: Duration = Duration.ZERO) {
    showTitle(Title.title(main, sub, Title.Times.times(fadeIn.toJavaDuration(), stay.toJavaDuration(), fadeOut.toJavaDuration())))
}

fun Audience.toggle(current: Boolean): Boolean {
    return if (current) {
        soundDisable()
        false
    } else {
        soundEnable()
        true
    }
}