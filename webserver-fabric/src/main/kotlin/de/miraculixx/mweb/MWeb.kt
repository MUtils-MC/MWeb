package de.miraculixx.mweb

import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Server

fun init() {
    Events.Server.postStart.listen { event ->
        consoleAudience.sendMessage(prefix + cmp("Hello Server"))
    }

    Events.Server.preStop.listen {
        consoleAudience.sendMessage(prefix + cmp("Successfully saved all data! Good Bye :)"))
    }
}
