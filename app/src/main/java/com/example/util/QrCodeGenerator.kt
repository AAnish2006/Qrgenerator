package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

object QrCodeGenerator {

    /**
     * Generates a QR Code Bitmap with optional logo and custom colors.
     */
    fun generate(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        logo: Bitmap? = null,
        roundedDots: Boolean = false
    ): Bitmap {
        val hints = Hashtable<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        // Higher error correction needed if we overlay a logo in the center
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        
        // Draw background
        val bgPaint = Paint().apply { color = backgroundColor }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw bits
        val fgPaint = Paint().apply {
            color = foregroundColor
            isAntiAlias = true
        }

        val dotWidth = width / bitMatrix.width.toFloat()
        val dotHeight = height / bitMatrix.height.toFloat()

        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                if (bitMatrix.get(x, y)) {
                    val left = x * dotWidth
                    val top = y * dotHeight
                    val right = left + dotWidth
                    val bottom = top + dotHeight

                    if (roundedDots) {
                        // Drawing circles for standard dots (Premium style)
                        canvas.drawRoundRect(
                            left, top, right, bottom, 
                            dotWidth / 2f, dotHeight / 2f, 
                            fgPaint
                        )
                    } else {
                        // Standard square pixels
                        canvas.drawRect(left, top, right, bottom, fgPaint)
                    }
                }
            }
        }

        // Draw Logo if specified (Premium option)
        if (logo != null) {
            val logoWidth = width / 5
            val logoHeight = height / 5
            val logoLeft = (width - logoWidth) / 2f
            val logoTop = (height - logoHeight) / 2f

            val scaledLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true)
            
            // Draw a tiny white background card behind the logo
            val cardPaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            val borderSize = 4f
            canvas.drawRoundRect(
                logoLeft - borderSize, 
                logoTop - borderSize, 
                logoLeft + logoWidth + borderSize, 
                logoTop + logoHeight + borderSize, 
                8f, 8f, 
                cardPaint
            )

            canvas.drawBitmap(scaledLogo, logoLeft, logoTop, null)
        }

        return bmp
    }
}
