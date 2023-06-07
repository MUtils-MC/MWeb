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
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.axay.kspigot.items.customModel
import net.axay.kspigot.runnables.sync
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.io.path.Path

class ActionFilesManage : GUIEvent, ActionFiles, FileManaging {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event
        val meta = item.itemMeta ?: return@event
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (meta.customModel) {
            100 -> {
                val path = meta.persistentDataContainer.get(provider.pathNamespace) ?: provider.currentFolder.path
                val file = Path(path)

                when (it.click) {
                    ClickType.LEFT -> file.navigate(player, provider, inv)

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
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
                                        Zipping.zipFolder(file, Path("$path.zip"))
                                        sync {
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
                                    player.closeInventory()
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
                                    Zipping.unzipArchive(file, Path(path.removeSuffix(".${file.extension}")))
                                    sync {
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