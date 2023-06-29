@file:Suppress("UNCHECKED_CAST")

package de.miraculixx.mweb.commands

import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.interfaces.FileManaging
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.enumOf
import de.miraculixx.mweb.MWeb
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mweb.gui.actions.ActionFilesManage
import de.miraculixx.mweb.gui.actions.ActionFilesUpload
import de.miraculixx.mweb.gui.actions.ActionFilesWhitelist
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.module.permVisual
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.axay.kspigot.extensions.console
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.io.File
import kotlin.time.Duration

class MainCommand : WhitelistHandling, FileManaging {
    val command = commandTree("webserver") {
        withAliases("ws")
        playerExecutor { player, _ ->
            if (!player.permVisual("mweb.manage.list")) return@playerExecutor
            player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
            GUITypes.FILE_MANAGE.buildInventory(player, "${player.uniqueId}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
        }

        argument(LiteralArgument("whitelist").withPermission("mweb.whitelist.list")) {
            playerExecutor { player, _ ->
                player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
                GUITypes.FILE_WHITELISTING.buildInventory(player, "${player.uniqueId}-WHITELIST", ItemFilesManage(File("./"), GUITypes.FILE_WHITELISTING), ActionFilesWhitelist())
            }
            argument(LiteralArgument("add").withPermission("mweb.whitelist.custom")) {
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

            argument(LiteralArgument("remove").withPermission("mweb.whitelist.delete")) {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips {
                    ServerData.getWhitelists().map { StringTooltip.ofString(it.key, it.value.path) }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = args[0] as String
                        sender.removeWhitelist(id)
                    }
                }
            }

            argument(LiteralArgument("get").withPermission("mweb.whitelist.info")) {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips {
                    ServerData.getWhitelists().map { StringTooltip.ofString(it.key, it.value.path) }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = args[0] as String
                        ServerData.getFileData(id)?.let { sender.printLink(it, id) } ?: sender.sendMessage(prefix + cmp(msgString("event.idNotFound", listOf(id)), cError))
                    }
                }
            }
        }

        argument(LiteralArgument("upload").withPermission("mweb.upload.list")) {
            playerExecutor { player, _ ->
                player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
                GUITypes.FILE_UPLOADING.buildInventory(player, "${player.uniqueId}-UPLOAD", ItemFilesManage(File("./"), GUITypes.FILE_UPLOADING), ActionFilesUpload())
            }
            argument(LiteralArgument("add").withPermission("mweb.upload.custom")) {
                stringArgument("file") {
                    argument(StringArgument("access").replaceSuggestions(ArgumentSuggestions.strings(WhitelistType.values().map { it.name }))) {
                        integerArgument("maxAmount", 1) {
                            longArgument("maxSize", 1) {
                                anyExecutor { sender, args ->
                                    val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                    sender.whitelistUpload(args[0] as String, access, null, args[3] as Long, args[2] as Int)
                                }
                                stringArgument("restriction") {
                                    anyExecutor { sender, args ->
                                        val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                        sender.whitelistUpload(args[0] as String, access, args[4] as String, args[3] as Long, args[2] as Int)
                                    }
                                    stringArgument("timeout") {
                                        anyExecutor { sender, args ->
                                            val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                            val timeout = Duration.parse(args[3] as String)
                                            sender.whitelistUpload(args[0] as String, access, args[4] as String, args[3] as Long, args[2] as Int, timeout)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            argument(LiteralArgument("remove").withPermission("mweb.upload.delete")) {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips {
                    ServerData.getUploads().map { StringTooltip.ofString(it.key, it.value.path) }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        sender.removeUpload(args[0] as String)
                    }
                }
            }

            argument(LiteralArgument("get").withPermission("mweb.upload.info")) {
                argument(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips {
                    ServerData.getUploads().map { StringTooltip.ofString(it.key, it.value.path) }.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val id = args[0] as String
                        ServerData.getUploadData(id)?.let { sender.printLink(it, id) } ?: sender.sendMessage(prefix + cmp(msgString("event.idNotFound", listOf(id)), cError))
                    }
                }
            }
        }

        argument(LiteralArgument("manage").withPermission("mweb.manage.list")) {
            playerExecutor { player, _ ->
                player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
                GUITypes.FILE_MANAGE.buildInventory(player, "${player.uniqueId}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
            }
            argument(LiteralArgument("rename").withPermission("mweb.manage.rename")) {
                textArgument("file") {
                    stringArgument("new-name") {
                        anyExecutor { sender, args ->
                            val path = args[0] as String
                            val newName = args[1] as String
                            sender.renameFile(path, newName)
                        }
                    }
                }
            }

            argument(LiteralArgument("delete").withPermission("mweb.manage.delete")) {
                textArgument("file") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        sender.deleteFile(path)
                    }
                }
            }

            argument(LiteralArgument("zip").withPermission("mweb.manage.zip")) {
                textArgument("file") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        sender.zipFolder(path)
                    }
                }
            }

            argument(LiteralArgument("unzip").withPermission("mweb.manage.zip")) {
                textArgument("file") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        sender.unzipFolder(path)
                    }
                }
            }
        }

        argument(LiteralArgument("texturepack").withPermission("mweb.texturepack.send")) {
            textArgument("path") {
                entitySelectorArgumentManyPlayers("target") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        val targets = args[1] as List<Player>
                        sender.loadTP(path, targets, false)
                    }
                    booleanArgument("force") {
                        anyExecutor { sender, args ->
                            val path = args[0] as String
                            val targets = args[1] as List<Player>
                            val force = args[2] as Boolean
                            sender.loadTP(path, targets, force)
                        }
                    }
                }
            }
        }

        argument(LiteralArgument("settings").withPermission("mweb.settings")) {
            literalArgument("port") {
                anyExecutor { sender, _ ->
                    sender.sendMessage(prefix + cmp("Current Port: ") + cmp("${settings.port}", cMark))
                }
                integerArgument("port", 0, 65535) {
                    anyExecutor { sender, args ->
                        val newValue = args[0] as Int
                        settings.port = newValue
                        sender.sendMessage(prefix + cmp("The port is now ") + cmp("$newValue", cMark))
                    }
                }
            }

            literalArgument("logaccess") {
                anyExecutor { sender, _ ->
                    sender.sendMessage(prefix + cmp("Log file access: ") + cmp(settings.logAccess.msg(), cMark))
                }
                booleanArgument("logaccess") {
                    anyExecutor { sender, args ->
                        val newValue = args[0] as Boolean
                        settings.logAccess = newValue
                        sender.sendMessage(prefix + cmp("Logging file access is now ") + cmp(newValue.msg(), cMark))
                    }
                }
            }

            literalArgument("debug") {
                anyExecutor { sender, _ ->
                    sender.sendMessage(prefix + cmp("Debuging mode: ") + cmp(settings.debug.msg(), cMark))
                }
                booleanArgument("debug") {
                    anyExecutor { sender, args ->
                        val newValue = args[0] as Boolean
                        settings.debug = newValue
                        sender.sendMessage(prefix + cmp("Debugging mode is now ") + cmp(newValue.msg(), cMark))
                    }
                }
            }

            literalArgument("proxy") {
                anyExecutor { sender, _ ->
                    sender.sendMessage(prefix + cmp("Current proxy link: ") + cmp(settings.proxy ?: msgNone, cMark))
                }
                textArgument("proxy") {
                    anyExecutor { sender, args ->
                        val newValue = args[0] as String
                        settings.proxy = newValue
                        sender.sendMessage(prefix + cmp("All links now use ") + cmp(newValue, cMark))
                        sender.sendMessage(prefix + cmp("Make sure you can see the MWeb screen under ") + cmp(newValue, cMark, underlined = true).clickEvent(ClickEvent.openUrl(newValue)))
                        sender.sendMessage(prefix + cmp("Issues? Ask us at ") + cmp("dc.mutils.net", cMark, underlined = true).clickEvent(ClickEvent.openUrl("https://dc.mutils.net")))
                    }
                }
            }

            literalArgument("language") {
                anyExecutor { sender, _ ->
                    sender.sendMessage(prefix + cmp("Current language: ") + cmp(settings.lang, cMark))
                }
                stringArgument("language") {
                    anyExecutor { sender, args ->
                        val newValue = args[0] as String
                        if (MWeb.localization.setLanguage(newValue)) {
                            settings.lang = newValue
                            sender.sendMessage(prefix + cmp(msgString("command.switchLang")))
                        } else {
                            sender.sendMessage(prefix + cmp("Failed to apply language $newValue! Please check if $newValue.yml exist in the language folder", cError))
                        }
                    }
                }
            }
        }
    }

    private fun Audience.loadTP(path: String, targets: List<Player>, force: Boolean) {
        val rpInfo = createResourcePackAccess(path) ?: return
        taskRunLater(20 * 60) {
            console.removeWhitelist(rpInfo.data)
        }
        targets.forEach { player ->
            player.setResourcePack(rpInfo.link, rpInfo.hash, rpInfo.prompt, force)
        }
    }
}