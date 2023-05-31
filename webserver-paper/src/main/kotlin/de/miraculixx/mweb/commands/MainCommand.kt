package de.miraculixx.mweb.commands

import de.miraculixx.mvanilla.commands.WhitelistHandling
import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.WhitelistType
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.serializer.enumOf
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.gui.actions.ActionFilesManage
import de.miraculixx.mweb.gui.actions.ActionFilesWhitelist
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Sound
import java.io.File
import kotlin.time.Duration

class MainCommand : WhitelistHandling {
    val command = commandTree("webserver") {
        withAliases("ws")
        playerExecutor { player, _ ->
            player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
            GUITypes.FILE_MANAGE.buildInventory(player, "${player.uniqueId}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
        }

        argument(LiteralArgument("whitelist").withPermission("webserver.whitelist")) {
            playerExecutor { player, _ ->
                player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
                GUITypes.FILE_WHITELISTING.buildInventory(player, "${player.uniqueId}-WHITELIST", ItemFilesManage(File("./"), GUITypes.FILE_WHITELISTING), ActionFilesWhitelist())
            }
            literalArgument("add") {
                stringArgument("file") {
                    argument(StringArgument("access").replaceSuggestions(ArgumentSuggestions.strings(WhitelistType.values().map { it.name }))) {
                        anyExecutor { sender, args ->
                            val path = args[0] as String
                            val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                            sender.whitelistFile(path, access)
                        }
                        stringArgument("restriction") {
                            anyExecutor { sender, args ->
                                val path = args[0] as String
                                val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                val restriction = args[2] as String
                                sender.whitelistFile(path, access, restriction)
                            }
                            stringArgument("timeout") {
                                anyExecutor { sender, args ->
                                    val path = args[0] as String
                                    val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                    val restriction = args[2] as String
                                    val timeout = Duration.parse(args[3] as String)
                                    sender.whitelistFile(path, access, restriction, timeout)
                                }
                            }
                        }
                    }
                }
            }

            literalArgument("remove") {
                argument(StringArgument("file").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(ServerData.getWhitelists().map { StringTooltip.ofString(it.first, it.second) }))) {
                    anyExecutor { sender, args ->
                        val id = args[0] as String
                        sender.removeWhitelist(id)
                    }
                }
            }
        }
    }

    private fun toUrl(path: String): String {
        val isFolder = File(path).isDirectory
        return "http://${WebServer.publicIP}:${settings.port}/download?path=$path${if (isFolder) "&zip=true" else ""}"
    }
}