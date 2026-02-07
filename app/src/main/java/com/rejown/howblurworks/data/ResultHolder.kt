package com.rejown.howblurworks.data

import android.graphics.Bitmap
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize

/**
 * Singleton to hold result data between Visualization and Result screens.
 * Bitmaps cannot be passed through navigation arguments, so we use this holder.
 */
object ResultHolder {
    // Input bitmap for sample images (set by HomeScreen before navigation)
    var inputBitmap: Bitmap? = null

    var originalBitmap: Bitmap? = null
    var blurredBitmap: Bitmap? = null
    var blurType: BlurType = BlurType.GAUSSIAN
    var kernelSize: KernelSize = KernelSize.SIZE_3
    var imageWidth: Int = 0
    var imageHeight: Int = 0
    var processedPixels: Int = 0
    var processingTimeMs: Long = 0

    fun setResult(
        original: Bitmap?,
        blurred: Bitmap?,
        type: BlurType,
        size: KernelSize,
        width: Int,
        height: Int,
        pixels: Int,
        timeMs: Long
    ) {
        originalBitmap = original
        blurredBitmap = blurred
        blurType = type
        kernelSize = size
        imageWidth = width
        imageHeight = height
        processedPixels = pixels
        processingTimeMs = timeMs
    }

    fun clear() {
        inputBitmap = null
        originalBitmap = null
        blurredBitmap = null
        blurType = BlurType.GAUSSIAN
        kernelSize = KernelSize.SIZE_3
        imageWidth = 0
        imageHeight = 0
        processedPixels = 0
        processingTimeMs = 0
    }

    fun clearInput() {
        inputBitmap = null
    }
}
