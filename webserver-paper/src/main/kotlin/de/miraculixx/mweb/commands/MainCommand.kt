package de.miraculixx.mweb.commands

import de.miraculixx.mvanilla.commands.MainCommandInstance
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.web.WebServer
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.event.ClickEvent
import java.io.File

class MainCommand: MainCommandInstance {
    val command = commandTree("webserver") {
        withAliases("ws")
        playerExecutor { player, _ ->

        }

        argument(LiteralArgument("whitelist").withPermission("webserver.whitelist")) {
            literalArgument("add") {
                stringArgument("file") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        if (ServerData.getWhitelistedFiles().add(path)) {
                            val url = toUrl(path)
                            sender.sendMessage(
                                prefix + cmp("The file ") + cmp(path, cHighlight) + cmp(" is now downloadable via ") +
                                        cmp(url, cHighlight).clickEvent(ClickEvent.openUrl(url)).addHover(cmp("Click to download file"))
                            )
                        } else {
                            sender.sendMessage(prefix + cmp("The file ", cError) + cmp(path, cError, underlined = true) + cmp(" is already whitelisted!", cError))
                        }
                    }
                }
            }

            literalArgument("remove") {
                argument(StringArgument("file").replaceSuggestions(ArgumentSuggestions.strings(ServerData.getWhitelistedFiles()))) {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        if (ServerData.getWhitelistedFiles().remove(path)) {
                            sender.sendMessage(prefix + cmp("The file ") + cmp(path, cHighlight) + cmp(" is now privat! The download will stop working (Note, some browsers cache files!)"))
                        } else {
                            sender.sendMessage(prefix + cmp("Your selected file is already privat!", cError))
                        }
                    }
                }
            }
        }

        argument(LiteralArgument("downloads").withPermission("webserver.downloads")) {
            playerExecutor { player, _ ->

            }
            argument(StringArgument("file").replaceSuggestions(ArgumentSuggestions.strings(ServerData.getWhitelistedFiles()))) {
                anyExecutor { sender, args ->
                    val path = args[0] as String
                    val url = toUrl(path)
                    sender.sendMessage(prefix + cmp("Download - ") + cmp(url, cMark).clickEvent(ClickEvent.openUrl(url)).addHover(cmp("Click to download file")))
                }
            }
        }
    }

    private fun toUrl(path: String): String {
        val isFolder = File(path).isDirectory
        return "http://${WebServer.publicIP}:${settings.port}/download?path=$path${if (isFolder) "&zip=true" else ""}"
    }
}