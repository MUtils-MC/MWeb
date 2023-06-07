package de.miraculixx.mvanilla.interfaces

import de.miraculixx.mvanilla.data.FileType
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.Zipping
import net.kyori.adventure.audience.Audience
import java.io.File

interface FileManaging {
    fun Audience.renameFile(path: String, newName: String) {
        val file = File(path)
        if (!fileExist(file)) return
        val parent = file.parentFile
        try {
            if (parent == null) file.renameTo(File(newName))
            else file.renameTo(File(file.parentFile, newName))
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
        val file = File(path)
        if (!fileExist(file)) return
        try {
            if (file.isDirectory) file.deleteRecursively()
            else file.delete()
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
        val file = File(path)
        if (!file.isDirectory) {
            sendMessage(prefix + cmp(msgString("event.noFolder"), cError))
            return
        }
        sendMessage(prefix + cmp(msgString("event.startZip")))
        Zipping.zipFolder(file, File("$path.zip"))
        soundEnable()
        sendMessage(prefix + cmp(msgString("event.finishZip", listOf(file.name))))
    }

    fun Audience.unzipFolder(path: String) {
        val file = File(path)
        if (!fileExist(file)) return
        val type = FileType.getType(file.extension)
        if (type != FileType.ARCHIVE) {
            sendMessage(prefix + cmp(msgString("event.noArchive"), cError))
            return
        }
        sendMessage(prefix + cmp(msgString("event.startZip")))
        Zipping.unzipArchive(file, File(path.removeSuffix(".${file.extension}")))
        soundEnable()
        sendMessage(prefix + cmp(msgString("event.finishZip", listOf(file.name))))
    }

    private fun Audience.fileExist(file: File): Boolean {
        return if (!file.exists()) {
            soundError()
            sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(file.path))))
            false
        } else true
    }
}