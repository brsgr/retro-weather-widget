package com.example.weatherwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val padding = sizePx * 0.05f

        // Draw yellow/amber triangle
        val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFAA00.toInt()
            style = Paint.Style.FILL
        }
        val path = Path().apply {
            moveTo(sizePx / 2f, padding)
            lineTo(sizePx - padding, sizePx - padding)
            lineTo(padding, sizePx - padding)
            close()
        }
        canvas.drawPath(path, trianglePaint)

        // Draw dark exclamation mark
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF222222.toInt()
            textSize = sizePx * 0.55f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = sizePx * 0.82f
        canvas.drawText("!", sizePx / 2f, textY, textPaint)

        return bitmap
    }
}
