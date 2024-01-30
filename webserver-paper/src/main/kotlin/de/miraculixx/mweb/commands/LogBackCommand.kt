package de.miraculixx.mweb.commands

import de.miraculixx.mvanilla.interfaces.LogPayloads
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.stringArgument
import net.kyori.adventure.audience.Audience

class LogBackCommand : LogPayloads {
    private val confirmations: MutableMap<Audience, String> = mutableMapOf()
    private val cooldown: MutableSet<String> = mutableSetOf()

    private val command = commandTree("mlogs") {
        anyExecutor { sender, _ ->
            sender.commandResponseInfo("plugin")
        }

        stringArgument("plugin") {
            anyExecutor { sender, args ->
                sender.commandResponseMod(args[0] as String, "plugin")
            }

            stringArgument("code") {
                anyExecutor { sender, args ->
                    val plugin = args[0] as String
                    val code = args[1] as String
                    sender.commandResponseCode(plugin, code, cooldown, confirmations)
                }
            }
        }
    }
}