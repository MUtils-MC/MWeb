package de.miraculixx.mweb.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.interfaces.FileManaging
import de.miraculixx.mvanilla.interfaces.WhitelistHandling
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.enumOf
import de.miraculixx.mweb.api.data.WhitelistType
import de.miraculixx.mweb.gui.actions.ActionFilesManage
import de.miraculixx.mweb.gui.actions.ActionFilesWhitelist
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.logic.item.native
import de.miraculixx.mweb.localization
import de.miraculixx.mweb.module.permVisual
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.event.ClickEvent
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.task.mcCoroutineTask
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class MainCommand : WhitelistHandling, FileManaging {
    val command = command("webserver") {
        alias("ws")
        runs {
            val player = source.player ?: return@runs
            if (!player.permVisual("mweb.manage.list")) return@runs
            player.playSound(Sound.sound(Key.key("block.ender_chest.open"), Sound.Source.MASTER, 0.5f, 1f))
            GUITypes.FILE_MANAGE.buildInventory(player, "${player.uuid}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
        }

        literal("whitelist") {
            requires { it.player?.permVisual("mweb.whitelist.list") ?: true }
            runs {
                val player = source.player ?: return@runs
                player.playSound(Sound.sound(Key.key("block.ender_chest.open"), Sound.Source.MASTER, 0.5f, 1f))
                GUITypes.FILE_WHITELISTING.buildInventory(player, "${player.uuid}-WHITELIST", ItemFilesManage(File("./"), GUITypes.FILE_WHITELISTING), ActionFilesWhitelist())
            }
            literal("add") {
                requires { it.player?.permVisual("mweb.whitelist.custom") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    argument<String>("access", StringArgumentType.string()) { access ->
                        suggestList { WhitelistType.entries.map { it.name } }
                        runs {
                            val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                            source.whitelistFile(file(), acc)
                        }
                        argument<String>("restriction", StringArgumentType.string()) { restriction ->
                            runs {
                                val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                                source.whitelistFile(file(), acc, restriction())
                            }
                            argument<String>("timeout", StringArgumentType.string()) { timeout ->
                                runs {
                                    val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                                    val timed = try {
                                        Duration.parse(timeout())
                                    } catch (_: Exception) {
                                        source.soundError()
                                        return@runs
                                    }
                                    source.whitelistFile(file(), acc, restriction(), timed)
                                }
                            }
                        }
                    }
                }
            }

            literal("remove") {
                requires { it.player?.permVisual("mweb.whitelist.delete") ?: true }
                argument<String>("id", StringArgumentType.string()) { id ->
                    suggestListWithTooltips { ServerData.getWhitelists().map { it.key to Component.literal(it.value.path) }.asIterable() }
                    runs {
                        source.removeWhitelist(id())
                    }
                }
            }

            literal("get") {
                requires { it.player?.permVisual("mweb.whitelist.info") ?: true }
                argument<String>("id", StringArgumentType.string()) { id ->
                    suggestListWithTooltips { ServerData.getWhitelists().map { it.key to Component.literal(it.value.path) }.asIterable() }
                    runs {
                        val stringID = id()
                        ServerData.getFileData(stringID)?.let { source.printLink(it, stringID) } ?: source.sendMessage(prefix + cmp(msgString("event.idNotFound", listOf(stringID)), cError))
                    }
                }
            }
        }

        literal("upload") {
            requires { it.player?.permVisual("mweb.upload.list") ?: true }
            runs {
                val player = source.player ?: return@runs
                player.playSound(Sound.sound(Key.key("block.ender_chest.open"), Sound.Source.MASTER, 0.5f, 1f))
                GUITypes.FILE_UPLOADING.buildInventory(player, "${player.uuid}-WHITELIST", ItemFilesManage(File("./"), GUITypes.FILE_UPLOADING), ActionFilesWhitelist())
            }
            literal("add") {
                requires { it.player?.permVisual("mweb.whitelist.custom") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    argument<String>("access", StringArgumentType.string()) { access ->
                        suggestList { WhitelistType.entries.map { it.name } }
                        runs {
                            val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                            source.whitelistFile(file(), acc)
                        }
                        argument<String>("restriction", StringArgumentType.string()) { restriction ->
                            runs {
                                val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                                source.whitelistFile(file(), acc, restriction())
                            }
                            argument<String>("timeout", StringArgumentType.string()) { timeout ->
                                runs {
                                    val acc = enumOf<WhitelistType>(access()) ?: WhitelistType.GLOBAL
                                    val timed = try {
                                        Duration.parse(timeout())
                                    } catch (_: Exception) {
                                        source.soundError()
                                        return@runs
                                    }
                                    source.whitelistFile(file(), acc, restriction(), timed)
                                }
                            }
                        }
                    }
                }
            }

            literal("remove") {
                requires { it.player?.permVisual("mweb.whitelist.delete") ?: true }
                argument<String>("id", StringArgumentType.string()) { id ->
                    suggestListWithTooltips { ServerData.getWhitelists().map { it.key to Component.literal(it.value.path) }.asIterable() }
                    runs {
                        source.removeWhitelist(id())
                    }
                }
            }

            literal("get") {
                requires { it.player?.permVisual("mweb.whitelist.info") ?: true }
                argument<String>("id", StringArgumentType.string()) { id ->
                    suggestListWithTooltips { ServerData.getWhitelists().map { it.key to Component.literal(it.value.path) }.asIterable() }
                    runs {
                        val stringID = id()
                        ServerData.getFileData(stringID)?.let { source.printLink(it, stringID) } ?: source.sendMessage(prefix + cmp(msgString("event.idNotFound", listOf(stringID)), cError))
                    }
                }
            }
        }

        literal("manage") {
            requires { it.player?.permVisual("mweb.manage.list") ?: true }
            runs {
                val player = source.player ?: return@runs
                player.playSound(Sound.sound(Key.key("block.ender_chest.open"), Sound.Source.MASTER, 0.5f, 1f))
                GUITypes.FILE_MANAGE.buildInventory(player, "${player.uuid}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
            }
            literal("rename") {
                requires { it.player?.permVisual("mweb.manage.rename") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    argument<String>("new-name", StringArgumentType.string()) { newName ->
                        runs {
                            source.renameFile(file(), newName())
                        }
                    }
                }
            }

            literal("delete") {
                requires { it.player?.permVisual("mweb.manage.delete") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    runs {
                        source.deleteFile(file())
                    }
                }
            }

            literal("zip") {
                requires { it.player?.permVisual("mweb.manage.zip") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    runs {
                        source.zipFolder(file())
                    }
                }
            }

            literal("unzip") {
                requires { it.player?.permVisual("mweb.manage.zip") ?: true }
                argument<String>("file", StringArgumentType.string()) { file ->
                    runs {
                        source.unzipFolder(file())
                    }
                }
            }
        }

        literal("texturepack") {
            requires { it.player?.permVisual("mweb.texturepack.send") ?: true }
            argument<String>("path", StringArgumentType.string()) { file ->
                argument<EntitySelector>("target", EntityArgument.players()) { target ->
                    runs {
                        val targets = target().findPlayers(source)
                        source.loadTP(file(), targets, false)
                    }
                    argument<Boolean>("force") { force ->
                        runs {
                            val targets = target().findPlayers(source)
                            source.loadTP(file(), targets, force())
                        }
                    }
                }
            }
        }

        literal("settings") {
            requires { it.player?.permVisual("mweb.settings") ?: true }
            literal("port") {
                runs {
                    source.sendMessage(prefix + cmp("Current Port: ") + cmp("${settings.port}", cMark))
                }
                argument<Int>("port", IntegerArgumentType.integer(0, 65535)) { port ->
                    runs {
                        val newValue = port()
                        settings.port = newValue
                        source.sendMessage(prefix + cmp("The port is now ") + cmp("$newValue", cMark))
                    }
                }
            }

            literal("logaccess") {
                runs {
                    source.sendMessage(prefix + cmp("Log file access: ") + cmp(settings.logAccess.msg(), cMark))
                }
                argument<Boolean>("logaccess") { logaccess ->
                    runs {
                        val newValue = logaccess()
                        settings.logAccess = newValue
                        source.sendMessage(prefix + cmp("Logging file access is now ") + cmp(newValue.msg(), cMark))
                    }
                }
            }

            literal("debug") {
                runs {
                    source.sendMessage(prefix + cmp("Debuging mode: ") + cmp(settings.debug.msg(), cMark))
                }
                argument<Boolean>("debug") { debug ->
                    runs {
                        val newValue = debug()
                        settings.debug = newValue
                        source.sendMessage(prefix + cmp("Debugging mode is now ") + cmp(newValue.msg(), cMark))
                    }
                }
            }

            literal("proxy") {
                runs {
                    source.sendMessage(prefix + cmp("Current proxy link: ") + cmp(settings.proxy ?: msgNone, cMark))
                }
                argument<String>("proxy", StringArgumentType.string()) { proxy ->
                    runs {
                        val newValue = proxy()
                        settings.proxy = newValue
                        source.sendMessage(prefix + cmp("All links now use ") + cmp(newValue, cMark))
                        source.sendMessage(prefix + cmp("Make sure you can see the MWeb screen under ") + cmp(newValue, cMark, underlined = true).clickEvent(ClickEvent.openUrl(newValue)))
                        source.sendMessage(prefix + cmp("Issues? Ask us at ") + cmp("dc.mutils.net", cMark, underlined = true).clickEvent(ClickEvent.openUrl("https://dc.mutils.net")))
                    }
                }
            }

            literal("language") {
                runs {
                    source.sendMessage(prefix + cmp("Current language: ") + cmp(settings.lang, cMark))
                }
                argument<String>("language", StringArgumentType.string()) { language ->
                    runs {
                        val newValue = language()
                        if (localization.setLanguage(newValue)) {
                            settings.lang = newValue
                            source.sendMessage(prefix + cmp(msgString("command.switchLang")))
                        } else {
                            source.sendMessage(prefix + cmp("Failed to apply language $newValue! Please check if $newValue.yml exist in the language folder", cError))
                        }
                    }
                }
            }
        }
    }

    private fun Audience.loadTP(path: String, targets: List<Player>, force: Boolean) {
        val whitelist = Audience.empty().whitelistFile(path, WhitelistType.GLOBAL)
        if (whitelist == null) {
            sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(path)), cError))
            return
        }
        mcCoroutineTask(delay = 1.minutes) {
            consoleAudience.removeWhitelist(whitelist.first)
        }
        val file = File(whitelist.second.zippedTo ?: whitelist.second.path)
        val hash = DigestUtils.getSha1Digest().digest(file.readBytes())

        val prompt = msg("event.texturepackPrompt", listOf(file.name))
        val link = ServerData.getLink(whitelist.first, true)
        targets.forEach { player ->
            (player as ServerPlayer).connection.send(ClientboundResourcePackPushPacket(UUID.randomUUID(), link, hash.decodeToString(), force, prompt.native()))
        }
    }
}