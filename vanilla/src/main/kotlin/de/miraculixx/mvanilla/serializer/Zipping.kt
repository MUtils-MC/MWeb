package de.miraculixx.mvanilla.serializer

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object Zipping {
    fun zipFolder(folder: File, zipFile: File) {
        val parentFolder = zipFile.parentFile
        if (!parentFolder.exists()) parentFolder.mkdir()
        val fos = FileOutputStream(zipFile)
        val zos = ZipOutputStream(fos)

        zipFolder(folder, "", zos)

        zos.close()
        fos.close()
    }

    private fun zipFolder(folder: File, parentPath: String, zos: ZipOutputStream) {
        val files = folder.listFiles() ?: return

        for (file in files) {
            if (file.extension == "lock") continue

            val relativePath = if (parentPath.isNotEmpty()) "$parentPath/${file.name}" else file.name

            if (file.isDirectory) {
                zipFolder(file, relativePath, zos)
            } else {
                val fis = FileInputStream(file)
                val entry = ZipEntry(relativePath)

                zos.putNextEntry(entry)

                val buffer = ByteArray(1024)
                var len: Int
                while (fis.read(buffer).also { len = it } > 0) {
                    zos.write(buffer, 0, len)
                }

                fis.close()
            }
        }
    }

    fun unzipArchive(zipFile: File, destFolder: File) {
        val destFolderPath = destFolder.path

        // Create the destination folder if it doesn't exist
        if (!destFolder.exists()) {
            destFolder.mkdirs()
        }

        val zipInputStream = ZipInputStream(zipFile.inputStream())
        var entry = zipInputStream.nextEntry

        while (entry != null) {
            val entryPath = destFolderPath + File.separator + entry.name
            val entryFile = File(entryPath)

            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile.mkdirs()

                val outputStream = FileOutputStream(entryFile)
                val buffer = ByteArray(1024)
                var len: Int

                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }

                outputStream.close()
            }

            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }

        zipInputStream.close()
    }
}