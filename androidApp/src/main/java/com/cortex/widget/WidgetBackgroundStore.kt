package com.cortex.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File

object WidgetBackgroundStore {
    private const val FILE_NAME = "widget_background.img"
    private const val MAX_DIMENSION = 320

    fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    fun save(context: Context, uri: Uri): Boolean {
        val target = file(context)
        val temp = File(context.cacheDir, "widget_background.tmp")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            } ?: return false
            if (target.exists()) target.delete()
            temp.renameTo(target)
        }.getOrDefault(false)
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    fun load(context: Context): Bitmap? {
        val file = file(context)
        if (!file.exists()) return null
        return runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                val width = info.size.width
                val height = info.size.height
                if (width > 0 && height > 0) {
                    val scale = minOf(
                        MAX_DIMENSION.toFloat() / width,
                        MAX_DIMENSION.toFloat() / height,
                        1f,
                    )
                    decoder.setTargetSize(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }.getOrNull()
    }
}
