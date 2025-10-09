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
}
