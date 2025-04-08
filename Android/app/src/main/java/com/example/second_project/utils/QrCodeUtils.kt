package com.example.second_project.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

object QrCodeUtils {
    private const val TAG = "QrCodeUtils_야옹"
    
    /**
     * 텍스트를 QR 코드로 변환하여 Bitmap으로 반환합니다.
     * 
     * @param text QR 코드에 인코딩할 텍스트
     * @param width QR 코드의 너비 (픽셀)
     * @param height QR 코드의 높이 (픽셀)
     * @return 생성된 QR 코드 Bitmap
     */
    fun generateQrCode(text: String, width: Int = 512, height: Int = 512): Bitmap? {
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1 // QR 코드 주변 여백 (1~4 사이 값 권장)
            
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            )
            
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
            
        } catch (e: WriterException) {
            android.util.Log.e(TAG, "QR 코드 생성 오류: ${e.message}")
            return null
        }
    }
} 