package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageUtil {

    private fun getMedicineImagesDir(context: Context): File {
        val dir = File(context.filesDir, "medicine_images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * ذخیره امن عکس انتخاب شده از گالری در حافظه دائمی برنامه
     */
    fun saveGalleryUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = getMedicineImagesDir(context)
            val fileName = "med_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(dir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ذخیره Bitmap ثبت شده توسط دوربین
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val dir = getMedicineImagesDir(context)
            val fileName = "med_cam_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(dir, fileName)

            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ایجاد Uri موقت برای باز کردن اپلیکیشن دوربین و ذخیره فول‌سایز
     */
    fun createTempCameraUri(context: Context): Pair<Uri, File> {
        val cacheDir = context.cacheDir
        val tempFile = File.createTempFile("cam_temp_", ".jpg", cacheDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return Pair(uri, tempFile)
    }

    /**
     * انتقال فایل عکس دوربین موقت به حافظه دائمی
     */
    fun moveTempCameraFileToInternal(context: Context, tempFile: File): String? {
        return try {
            val dir = getMedicineImagesDir(context)
            val fileName = "med_cam_${System.currentTimeMillis()}.jpg"
            val destFile = File(dir, fileName)
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
