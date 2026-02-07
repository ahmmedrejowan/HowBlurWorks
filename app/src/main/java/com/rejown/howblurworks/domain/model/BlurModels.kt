package com.rejown.howblurworks.domain.model

import android.graphics.Bitmap

/**
 * Types of blur algorithms available
 */
enum class BlurType(val displayName: String) {
    GAUSSIAN("Gaussian"),
    BOX("Box"),
    MOTION_HORIZONTAL("Motion H"),
    MOTION_VERTICAL("Motion V")
}

/**
 * Available kernel sizes
 */
enum class KernelSize(val size: Int, val displayName: String) {
    SIZE_3(3, "3×3"),
    SIZE_5(5, "5×5"),
    SIZE_7(7, "7×7"),
    SIZE_9(9, "9×9"),
    SIZE_11(11, "11×11")
}

/**
 * Blur intensity presets
 */
enum class BlurIntensity(val displayName: String, val sigma: Float, val passes: Int) {
    LOW("Low", 0.8f, 1),
    MEDIUM("Medium", 1.5f, 2),
    HIGH("High", 2.5f, 3)
}

/**
 * Processing speed presets
 * Note: AUTO's duration is dynamically set from Settings, not the enum value
 */
enum class ProcessSpeed(val displayName: String, val targetDurationSec: Int) {
    SLOW("Slow", 45),
    AUTO("Auto", 30), // This value is overridden by Settings
    FAST("Fast", 15),
    INSTANT("Instant", 0)
}

/**
 * Auto-calculated speed configuration
 */
data class AutoSpeedConfig(
    val emitInterval: Int,
    val delayMs: Long,
    val totalFrames: Int,
    val estimatedDurationSec: Int
)

/**
 * Kernel configuration
 */
data class KernelConfig(
    val type: BlurType = BlurType.GAUSSIAN,
    val size: KernelSize = KernelSize.SIZE_3,
    val intensity: BlurIntensity = BlurIntensity.MEDIUM,
    val sigma: Float = intensity.sigma
)

/**
 * Single pixel value with position and color
 */
data class PixelValue(
    val x: Int,
    val y: Int,
    val color: Int
) {
    val red: Int get() = android.graphics.Color.red(color)
    val green: Int get() = android.graphics.Color.green(color)
    val blue: Int get() = android.graphics.Color.blue(color)
}

/**
 * Details of pixel calculation during convolution
 */
data class PixelCalculation(
    val centerPixel: PixelValue,
    val neighborPixels: List<PixelValue>,
    val weights: List<Float>,
    val resultColor: Int
) {
    val resultRed: Int get() = android.graphics.Color.red(resultColor)
    val resultGreen: Int get() = android.graphics.Color.green(resultColor)
    val resultBlue: Int get() = android.graphics.Color.blue(resultColor)
}

/**
 * Single step in the blur process
 */
data class BlurStep(
    val x: Int,
    val y: Int,
    val calculation: PixelCalculation?,
    val currentBitmap: Bitmap,
    val progress: Float,
    val processedPixels: Int,
    val totalPixels: Int
)

/**
 * Overall blur process state
 */
/**
 * Educational theory item for learning section
 */
data class BlurTheoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String, // Material icon name
    val content: String,
    val bulletPoints: List<String> = emptyList()
)

/**
 * Sample image for quick testing
 */
data class SampleImage(
    val id: String,
    val name: String,
    val drawableResId: Int,
    val description: String
)

/**
 * Kernel preview data for display
 */
data class KernelPreview(
    val matrix: List<List<String>>,
    val description: String,
    val blurType: BlurType,
    val size: KernelSize
)

/**
 * Overall blur process state
 */
data class BlurProcessState(
    val originalBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val displayBitmap: Bitmap? = null,
    val currentStep: BlurStep? = null,
    val kernelConfig: KernelConfig = KernelConfig(),
    val kernelMatrix: Array<FloatArray>? = null,
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isComplete: Boolean = false,
    val speed: ProcessSpeed = ProcessSpeed.AUTO,
    val autoSpeedConfig: AutoSpeedConfig? = null,
    val elapsedTimeMs: Long = 0,
    val error: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlurProcessState

        if (originalBitmap != other.originalBitmap) return false
        if (processedBitmap != other.processedBitmap) return false
        if (displayBitmap != other.displayBitmap) return false
        if (currentStep != other.currentStep) return false
        if (kernelConfig != other.kernelConfig) return false
        if (kernelMatrix != null) {
            if (other.kernelMatrix == null) return false
            if (!kernelMatrix.contentDeepEquals(other.kernelMatrix)) return false
        } else if (other.kernelMatrix != null) return false
        if (progress != other.progress) return false
        if (isRunning != other.isRunning) return false
        if (isPaused != other.isPaused) return false
        if (isComplete != other.isComplete) return false
        if (speed != other.speed) return false
        if (autoSpeedConfig != other.autoSpeedConfig) return false
        if (elapsedTimeMs != other.elapsedTimeMs) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalBitmap?.hashCode() ?: 0
        result = 31 * result + (processedBitmap?.hashCode() ?: 0)
        result = 31 * result + (displayBitmap?.hashCode() ?: 0)
        result = 31 * result + (currentStep?.hashCode() ?: 0)
        result = 31 * result + kernelConfig.hashCode()
        result = 31 * result + (kernelMatrix?.contentDeepHashCode() ?: 0)
        result = 31 * result + progress.hashCode()
        result = 31 * result + isRunning.hashCode()
        result = 31 * result + isPaused.hashCode()
        result = 31 * result + isComplete.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + (autoSpeedConfig?.hashCode() ?: 0)
        result = 31 * result + elapsedTimeMs.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
