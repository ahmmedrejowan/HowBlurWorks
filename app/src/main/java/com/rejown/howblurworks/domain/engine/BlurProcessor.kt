package com.rejown.howblurworks.domain.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.rejown.howblurworks.domain.model.AutoSpeedConfig
import com.rejown.howblurworks.domain.model.BlurStep
import com.rejown.howblurworks.domain.model.KernelConfig
import com.rejown.howblurworks.domain.model.PixelCalculation
import com.rejown.howblurworks.domain.model.PixelValue
import com.rejown.howblurworks.domain.model.ProcessSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * Visual blur processor that emits step-by-step progress
 */
class BlurProcessor {

    /**
     * Calculate auto-speed configuration based on image size
     */
    fun calculateAutoSpeed(
        width: Int,
        height: Int,
        speed: ProcessSpeed
    ): AutoSpeedConfig {
        val totalPixels = width * height
        val targetDurationSec = speed.targetDurationSec

        if (targetDurationSec == 0) {
            // Instant mode
            return AutoSpeedConfig(
                emitInterval = totalPixels,
                delayMs = 0,
                totalFrames = 1,
                estimatedDurationSec = 0
            )
        }

        val targetFps = 30
        val totalFrames = targetDurationSec * targetFps
        val emitInterval = maxOf(1, totalPixels / totalFrames)
        val delayMs = 1000L / targetFps

        return AutoSpeedConfig(
            emitInterval = emitInterval,
            delayMs = delayMs,
            totalFrames = minOf(totalFrames, totalPixels),
            estimatedDurationSec = targetDurationSec
        )
    }

    /**
     * Process image with step-by-step visualization
     */
    fun processWithVisualization(
        bitmap: Bitmap,
        config: KernelConfig,
        speedConfig: AutoSpeedConfig,
        startFromX: Int = 0,
        startFromY: Int = 0,
        currentOutputBitmap: Bitmap? = null
    ): Flow<BlurStep> = flow {
        val kernel = KernelGenerator.generate(config)
        val width = bitmap.width
        val height = bitmap.height
        val radius = config.size.size / 2
        val totalPixels = width * height

        // Use existing output bitmap if resuming, otherwise create new one
        val output = currentOutputBitmap?.copy(Bitmap.Config.ARGB_8888, true)
            ?: bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Get all pixels for faster access
        val inputPixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)
        bitmap.getPixels(inputPixels, 0, width, 0, 0, width, height)
        output.getPixels(outputPixels, 0, width, 0, 0, width, height)

        // Calculate starting position
        val startPixelIndex = startFromY * width + startFromX
        var processedCount = startPixelIndex
        var lastEmitCount = startPixelIndex

        for (y in 0 until height) {
            for (x in 0 until width) {
                // Skip already processed pixels when resuming
                val currentIndex = y * width + x
                if (currentIndex < startPixelIndex) continue

                if (!coroutineContext.isActive) return@flow

                // Perform convolution
                val calculation = convolve(
                    inputPixels, width, height, x, y, kernel, radius
                )

                // Update output pixel
                outputPixels[y * width + x] = calculation.resultColor
                processedCount++

                val progress = processedCount.toFloat() / totalPixels

                // Emit at configured intervals
                if (speedConfig.emitInterval > 0 &&
                    (processedCount - lastEmitCount >= speedConfig.emitInterval ||
                     processedCount == totalPixels)) {

                    lastEmitCount = processedCount

                    // Update bitmap with current progress
                    output.setPixels(outputPixels, 0, width, 0, 0, width, height)

                    emit(
                        BlurStep(
                            x = x,
                            y = y,
                            calculation = calculation,
                            currentBitmap = output.copy(Bitmap.Config.ARGB_8888, false),
                            progress = progress,
                            processedPixels = processedCount,
                            totalPixels = totalPixels
                        )
                    )

                    if (speedConfig.delayMs > 0) {
                        delay(speedConfig.delayMs)
                    }
                }
            }
        }

        // Final emit if not already emitted
        if (lastEmitCount < totalPixels) {
            output.setPixels(outputPixels, 0, width, 0, 0, width, height)
            emit(
                BlurStep(
                    x = width - 1,
                    y = height - 1,
                    calculation = null,
                    currentBitmap = output,
                    progress = 1f,
                    processedPixels = totalPixels,
                    totalPixels = totalPixels
                )
            )
        }

    }.flowOn(Dispatchers.Default)

    /**
     * Process entire image instantly (no visualization)
     */
    suspend fun processInstant(bitmap: Bitmap, config: KernelConfig): Bitmap {
        val kernel = KernelGenerator.generate(config)
        val width = bitmap.width
        val height = bitmap.height
        val radius = config.size.size / 2

        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val inputPixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)
        bitmap.getPixels(inputPixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val calculation = convolve(inputPixels, width, height, x, y, kernel, radius)
                outputPixels[y * width + x] = calculation.resultColor
            }
        }

        output.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Perform convolution at a single pixel
     */
    private fun convolve(
        pixels: IntArray,
        width: Int,
        height: Int,
        centerX: Int,
        centerY: Int,
        kernel: Array<FloatArray>,
        radius: Int
    ): PixelCalculation {
        var r = 0f
        var g = 0f
        var b = 0f
        val neighbors = mutableListOf<PixelValue>()
        val weights = mutableListOf<Float>()

        for (ky in -radius..radius) {
            for (kx in -radius..radius) {
                // Clamp to image bounds
                val px = (centerX + kx).coerceIn(0, width - 1)
                val py = (centerY + ky).coerceIn(0, height - 1)

                val pixel = pixels[py * width + px]
                val weight = kernel[ky + radius][kx + radius]

                r += Color.red(pixel) * weight
                g += Color.green(pixel) * weight
                b += Color.blue(pixel) * weight

                neighbors.add(PixelValue(px, py, pixel))
                weights.add(weight)
            }
        }

        val resultColor = Color.rgb(
            r.toInt().coerceIn(0, 255),
            g.toInt().coerceIn(0, 255),
            b.toInt().coerceIn(0, 255)
        )

        val centerPixel = pixels[centerY * width + centerX]

        return PixelCalculation(
            centerPixel = PixelValue(centerX, centerY, centerPixel),
            neighborPixels = neighbors,
            weights = weights,
            resultColor = resultColor
        )
    }
}
