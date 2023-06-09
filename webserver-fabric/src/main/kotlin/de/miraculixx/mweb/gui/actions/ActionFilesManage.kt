package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.interfaces.FileManaging
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.Zipping
import de.miraculixx.mweb.await.AwaitChatMessage
import de.miraculixx.mweb.await.AwaitConfirm
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.items.ItemLoading
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.data.GUIEvent
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.item.InventoryUtils.getID
import de.miraculixx.mweb.module.permVisual
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer
import net.silkmc.silk.core.task.mcCoroutineTask
import java.io.File

class ActionFilesManage : GUIEvent, ActionFiles, FileManaging {
    override val run: (GUIClickEvent, CustomInventory) -> Unit = event@{ it: GUIClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.player as? ServerPlayer ?: return@event
        val item = it.item
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (item.getID()) {
            100 -> {
                val path = item.getTagElement("de.miraculixx.api")?.getString(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)

                when (it.click) {
                    GUIClick.LEFT_CLICK -> file.navigate(player, provider, inv)

                    GUIClick.HOTKEY_SWAP -> {
                        when (it.clickId) {
                            //Rename
                            0 -> {
                                if (!player.permVisual("mweb.manage.rename")) return@event
                                player.click()
                                AwaitChatMessage(false, player, "Rename File", 60, file.name, true, msg("event.rename"), { msg ->
                                    player.renameFile(path, msg)
                                    inv.update()
                                }) {
                                    inv.open(player)
                                }
                            }
                            //ZIP Logic
                            1 -> {
                                if (!player.permVisual("mweb.manage.zip")) return@event
                                if (file.isDirectory) {
                                    GUITypes.LOADING.buildInventory(player, "LOADING", ItemLoading(), ActionEmpty())
                                    CoroutineScope(Dispatchers.Default).launch {
                                        Zipping.zipFolder(file, File("$path.zip"))
                                        mcCoroutineTask(true) {
                                            inv.update()
                                            inv.open(player)
                                            player.soundEnable()
                                        }
                                    }
                                } else player.soundStone()
                            }
                            //Delete
                            2 -> {
                                if (!player.permVisual("mweb.manage.delete")) return@event
                                player.click()
                                AwaitConfirm(player, {
                                    if (!player.permVisual("mweb.manage.delete", true)) return@AwaitConfirm
                                    player.deleteFile(path)
                                    player.closeContainer()
                                    inv.update()
                                    inv.open(player)
                                }) {
                                    player.click()
                                    inv.open(player)
                                }
                            }
                            //Unzip Logic
                            3 -> {
                                if (!player.permVisual("mweb.manage.zip")) return@event
                                val type = FileType.getType(file.extension)
                                if (type != FileType.ARCHIVE) {
                                    player.soundStone()
                                    return@event
                                }
                                player.click()
                                GUITypes.LOADING.buildInventory(player, "LOADING", ItemLoading(), ActionEmpty())
                                CoroutineScope(Dispatchers.Default).launch {
                                    Zipping.unzipArchive(file, File(path.removeSuffix(".${file.extension}")))
                                    mcCoroutineTask(true) {
                                        inv.update()
                                        inv.open(player)
                                        player.soundEnable()
                                    }
                                }
                            }

                            else -> player.soundStone()
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.soundStone()
            2 -> player.openWhitelist(provider)
            3 -> player.openUpload(provider)
        }
    }
}