package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object AudioStorageUtil {

    private fun getAlarmTonesDir(context: Context): File {
        val dir = File(context.filesDir, "alarm_tones")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "آهنگ انتخابی گوشی"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
        } catch (_: Exception) {
        }
        return name
    }

    fun saveCustomAudioUri(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val dir = getAlarmTonesDir(context)
            val displayName = getFileName(context, uri)
            val ext = if (displayName.contains(".")) displayName.substringAfterLast(".", "mp3") else "mp3"
            val destFile = File(dir, "custom_alarm_tone.$ext")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            Pair(destFile.absolutePath, displayName)
        } catch (e: Exception) {
            null
        }
    }

    fun getCustomAudioFile(context: Context): File? {
        val dir = getAlarmTonesDir(context)
        val files = dir.listFiles()
        return files?.firstOrNull { it.isFile && it.length() > 0 }
    }
}
