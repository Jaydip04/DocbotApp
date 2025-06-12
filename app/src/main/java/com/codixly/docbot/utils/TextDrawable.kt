package com.codixly.docbot.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.codixly.docbot.R

class TextDrawable(private val text: String, context: Context) : Drawable() {

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#1A02B3B7")
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
        textSize = 100f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = Math.min(bounds.width(), bounds.height()) / 2f

        // Draw circular background
        canvas.drawCircle(centerX, centerY, radius, bgPaint)

        // Draw text in center
        val yPos = centerY - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, centerX, yPos, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        bgPaint.alpha = alpha
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        bgPaint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
