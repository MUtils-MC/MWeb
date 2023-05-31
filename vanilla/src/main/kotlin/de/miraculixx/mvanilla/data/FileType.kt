package de.miraculixx.mvanilla.data

import de.miraculixx.mvanilla.messages.msgString
import java.time.Instant
import java.time.ZoneId

import java.time.format.DateTimeFormatter




enum class FileType(val desc: String) {
    ARCHIVE(msgString("event.fileType.archive")),
    JAR(msgString("event.fileType.jar")),
    CONFIGURATION(msgString("event.fileType.config")),
    DANGEROUS(msgString("event.fileType.dangerous")),
    MC_FILES(msgString("event.fileType.mc")),
    MEDIA_FILES(msgString("event.fileType.media")),

    DATA(msgString("event.fileType.default")),
    FOLDER(msgString("event.fileType.folder"))
    ;

    companion object {
        private const val pattern = "dd.MM.yyyy HH:mm:ss"
        fun getType(extension: String): FileType {
            return when (extension) {
                "zip", "rar", "tar", "gz" -> ARCHIVE
                "jar" -> JAR
                "json", "yml", "yaml", "toml", "conf" -> CONFIGURATION
                "properties", "lock" -> DANGEROUS
                "mcfunction", "mcmeta", "dat", "dat_old", "mca" -> MC_FILES
                "png", "jpg", "jpeg", "gif", "webp", "mov", "ogg", "mp4", "mp3" -> MEDIA_FILES
                else -> DATA
            }
        }

        fun getTime(instant: Instant): String {
            val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        }
    }
}