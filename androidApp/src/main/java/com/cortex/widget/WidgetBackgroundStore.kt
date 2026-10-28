package com.cortex.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File

data class WidgetBackground(
    val bitmap: Bitmap,
    val topColor: Int,
    val bottomColor: Int,
)

object WidgetBackgroundStore {
    private const val FILE_NAME = "widget_background.img"
    private const val MAX_DIMENSION = 320
    private const val FALLBACK_COLOR = 0xFF0A0D12.toInt()

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

    fun load(context: Context): WidgetBackground? {
        val file = file(context)
        if (!file.exists()) return null
        return runCatching {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
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
            WidgetBackground(
                bitmap = bitmap,
                topColor = edgeDominantColor(bitmap, fromTop = true),
                bottomColor = edgeDominantColor(bitmap, fromTop = false),
            )
        }.getOrNull()
    }

    private fun edgeDominantColor(bitmap: Bitmap, fromTop: Boolean): Int {
        val stripHeight = (bitmap.height / 32).coerceAtLeast(1)
        val y = if (fromTop) 0 else bitmap.height - stripHeight
        val pixels = IntArray(bitmap.width * stripHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, stripHeight)

        val bins = HashMap<Int, Int>()
        for (pixel in pixels) {
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha < 16) continue
            val r = (pixel ushr 16) and 0xFF
            val g = (pixel ushr 8) and 0xFF
            val b = pixel and 0xFF
            val key = ((r / 16) shl 8) or ((g / 16) shl 4) or (b / 16)
            bins[key] = (bins[key] ?: 0) + 1
        }

        val key = bins.maxByOrNull { it.value }?.key ?: return FALLBACK_COLOR
        val r = ((key shr 8) and 0xF) * 17
        val g = ((key shr 4) and 0xF) * 17
        val b = (key and 0xF) * 17
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }
}
