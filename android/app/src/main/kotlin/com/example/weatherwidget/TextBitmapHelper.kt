package com.example.weatherwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

object TextBitmapHelper {

    fun createTextBitmap(
        context: Context,
        text: String,
        textSizePx: Float,
        textColor: Int
    ): Bitmap {
        // Load custom font
        val typeface = try {
            ResourcesCompat.getFont(context, R.font.custom_font)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        // Create paint with custom font
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSizePx
            this.color = textColor
            this.textAlign = Paint.Align.LEFT
        }

        // Measure text
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top

        // Create bitmap with padding
        val padding = 4f
        val bitmapWidth = (textWidth + padding * 2).toInt()
        val bitmapHeight = (textHeight + padding * 2).toInt()

        val bitmap = Bitmap.createBitmap(
            bitmapWidth.coerceAtLeast(1),
            bitmapHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        // Draw text on bitmap
        val canvas = Canvas(bitmap)
        val x = padding
        val y = padding - fontMetrics.top
        canvas.drawText(text, x, y, paint)

        return bitmap
    }

    fun createWarningBitmap(sizePx: Int): Bitmap {
        // Draw at low resolution and scale up for a pixelated retro look
        val pixelSize = 12
        val smallBitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(smallBitmap)

        val amber = 0xFFFFAA00.toInt()
        val dark = 0xFF222222.toInt()

        // Pixel art warning triangle with exclamation mark
        val pixels = arrayOf(
            "     YY     ",
            "     YY     ",
            "    YYYY    ",
            "    YDDY    ",
            "   YYDDYY   ",
            "   YYDDYY   ",
            "  YYYDDYYY  ",
            "  YYY  YYY  ",
            " YYYYDDYYYY ",
            " YYYYYYYYYY ",
            "YYYYYYYYYYYY",
            "YYYYYYYYYYYY"
        )

        val paint = Paint().apply { isAntiAlias = false }
        for (y in pixels.indices) {
            for (x in pixels[y].indices) {
                when (pixels[y][x]) {
                    'Y' -> {
                        paint.color = amber
                        canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
                    }
                    'D' -> {
                        paint.color = dark
                        canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
                    }
                }
            }
        }

        // Scale up with nearest-neighbor interpolation for crisp pixel edges
        return Bitmap.createScaledBitmap(smallBitmap, sizePx, sizePx, false)
    }
}
