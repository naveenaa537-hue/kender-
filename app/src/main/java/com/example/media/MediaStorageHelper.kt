package com.example.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object MediaStorageHelper {

    fun copyUriToInternalStorage(context: Context, uri: Uri, subDir: String = "attachments"): StoredMediaInfo? {
        return try {
            val contentResolver = context.contentResolver
            var fileName = "file_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val storageDir = File(context.filesDir, subDir)
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val ext = fileName.substringAfterLast(".", "")
            val uniqueName = if (ext.isNotEmpty()) {
                "${UUID.randomUUID()}.$ext"
            } else {
                "${UUID.randomUUID()}"
            }

            val targetFile = File(storageDir, uniqueName)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(targetFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) {
                fileSize = targetFile.length()
            }

            StoredMediaInfo(
                localFilePath = targetFile.absolutePath,
                originalFileName = fileName,
                fileSize = fileSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getVoiceRecordingsDir(context: Context): File {
        val dir = File(context.filesDir, "voice_notes")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}

data class StoredMediaInfo(
    val localFilePath: String,
    val originalFileName: String,
    val fileSize: Long
)
