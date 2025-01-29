package com.example.examen1.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageManager(private val context: Context) {
    private val storageDir = context.getDir("images", Context.MODE_PRIVATE)

    suspend fun saveImage(uri: Uri, type: String, id: String): String {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Comprimir y redimensionar
            val resizedBitmap = resizeBitmap(bitmap, 1024f)
            val compressedBitmap = compressBitmap(resizedBitmap)

            // Crear directorio para el tipo si no existe
            val typeDir = File(storageDir, type)
            if (!typeDir.exists()) typeDir.mkdirs()

            // Guardar imagen
            val fileName = "${id}_${System.currentTimeMillis()}.jpg"
            val imageFile = File(typeDir, fileName)

            FileOutputStream(imageFile).use { out ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            imageFile.absolutePath
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Float): Bitmap {
        val ratio = maxSize / maxOf(bitmap.width, bitmap.height)
        return if (ratio < 1) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun deleteImage(path: String) {
        File(path).delete()
    }

    fun createTempFile(): File {
        return File.createTempFile(
            "TEMP_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
    }
}