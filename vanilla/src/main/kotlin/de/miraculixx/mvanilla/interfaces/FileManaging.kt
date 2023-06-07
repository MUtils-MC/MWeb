package de.miraculixx.mvanilla.interfaces

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.Zipping
import net.kyori.adventure.audience.Audience
import java.nio.file.Path
import kotlin.io.path.*

interface FileManaging {
    fun Audience.renameFile(path: String, newName: String) {
        val file = Path(path)
        if (!fileExist(file)) return
        val parent = file.parent
        try {
            if (parent == null) file.renameTo(File(newName))
            else file.
        } catch (e: Exception) {
            consoleAudience.sendMessage(prefix + cmp("Failed to rename file ${file.path}! Reason...", cError))
            consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown", cError))
            soundError()
            sendMessage(prefix + cmp(msgString("event.invalidName", listOf(newName))))
            return
        }
        soundEnable()
        sendMessage(prefix + cmp(msgString("event.setName", listOf(newName))))
        return
    }

    fun Audience.deleteFile(path: String) {
        val file = Path(path)
        if (!fileExist(file)) return
        try {
            if (file.isDirectory()) file.deleteRecursively()
            else file.deleteIfExists()
        } catch (e: Exception) {
            consoleAudience.sendMessage(prefix + cmp("Failed to delete file ${path}! Reason...", cError))
            consoleAudience.sendMessage(prefix + cmp(e.message ?: "Unknown", cError))
            sendMessage(prefix + cmp(msgString("event.invalidDelete", listOf(file.name)), cError))
            soundError()
            return
        }
        soundDelete()
    }

    fun Audience.zipFolder(path: String) {
        val file = Path(path)
        if (!file.isDirectory) {
            sendMessage(prefix + cmp(msgString("event.noFolder"), cError))
            return
        }
        sendMessage(prefix + cmp(msgString("event.startZip")))
        Zipping.zipFolder(file, Path("$path.zip"))
        soundEnable()
        sendMessage(prefix + cmp(msgString("event.finishZip", listOf(file.name))))
    }

    fun Audience.unzipFolder(path: String) {
        val file = Path(path)
        if (!fileExist(file)) return
        val type = FileType.getType(file.extension)
        if (type != FileType.ARCHIVE) {
            sendMessage(prefix + cmp(msgString("event.noArchive"), cError))
            return
        }
        sendMessage(prefix + cmp(msgString("event.startZip")))
        Zipping.unzipArchive(file, Path(path.removeSuffix(".${file.extension}")))
        soundEnable()
        sendMessage(prefix + cmp(msgString("event.finishZip", listOf(file.name))))
    }

    private fun Audience.fileExist(file: Path): Boolean {
        return if (!file.exists()) {
            soundError()
            sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(file.path))))
            false
        } else true
    }
}