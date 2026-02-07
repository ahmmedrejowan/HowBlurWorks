package com.rejown.howblurworks.presentation.visualization

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.rejown.howblurworks.data.ResultHolder
import androidx.lifecycle.viewModelScope
import com.rejown.howblurworks.domain.engine.BlurProcessor
import com.rejown.howblurworks.domain.engine.KernelGenerator
import com.rejown.howblurworks.domain.model.AutoSpeedConfig
import com.rejown.howblurworks.domain.model.BlurIntensity
import com.rejown.howblurworks.domain.model.BlurStep
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelConfig
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.PixelCalculation
import com.rejown.howblurworks.domain.model.ProcessSpeed
import com.rejown.howblurworks.util.BitmapUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisualizationUiState(
    val originalBitmap: Bitmap? = null,
    val displayBitmap: Bitmap? = null,
    val finalBitmap: Bitmap? = null,
    val kernelMatrix: Array<FloatArray>? = null,
    val kernelDisplayValues: List<List<String>> = emptyList(),
    val currentX: Int = 0,
    val currentY: Int = 0,
    val currentCalculation: PixelCalculation? = null,
    val progress: Float = 0f,
    val processedPixels: Int = 0,
    val totalPixels: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true,
    val speed: ProcessSpeed = ProcessSpeed.AUTO,
    val autoSpeedConfig: AutoSpeedConfig? = null,
    val elapsedTimeMs: Long = 0,
    val blurType: BlurType = BlurType.GAUSSIAN,
    val kernelSize: KernelSize = KernelSize.SIZE_3,
    val intensity: BlurIntensity = BlurIntensity.MEDIUM,
    val error: String? = null,
    // Pause/Resume state
    val pausedAtX: Int = 0,
    val pausedAtY: Int = 0,
    val pausedBitmap: Bitmap? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisualizationUiState

        if (originalBitmap != other.originalBitmap) return false
        if (displayBitmap != other.displayBitmap) return false
        if (finalBitmap != other.finalBitmap) return false
        if (kernelMatrix != null) {
            if (other.kernelMatrix == null) return false
            if (!kernelMatrix.contentDeepEquals(other.kernelMatrix)) return false
        } else if (other.kernelMatrix != null) return false
        if (kernelDisplayValues != other.kernelDisplayValues) return false
        if (currentX != other.currentX) return false
        if (currentY != other.currentY) return false
        if (currentCalculation != other.currentCalculation) return false
        if (progress != other.progress) return false
        if (processedPixels != other.processedPixels) return false
        if (totalPixels != other.totalPixels) return false
        if (isRunning != other.isRunning) return false
        if (isPaused != other.isPaused) return false
        if (isComplete != other.isComplete) return false
        if (isLoading != other.isLoading) return false
        if (speed != other.speed) return false
        if (autoSpeedConfig != other.autoSpeedConfig) return false
        if (elapsedTimeMs != other.elapsedTimeMs) return false
        if (blurType != other.blurType) return false
        if (kernelSize != other.kernelSize) return false
        if (intensity != other.intensity) return false
        if (error != other.error) return false
        if (pausedAtX != other.pausedAtX) return false
        if (pausedAtY != other.pausedAtY) return false
        if (pausedBitmap != other.pausedBitmap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalBitmap?.hashCode() ?: 0
        result = 31 * result + (displayBitmap?.hashCode() ?: 0)
        result = 31 * result + (finalBitmap?.hashCode() ?: 0)
        result = 31 * result + (kernelMatrix?.contentDeepHashCode() ?: 0)
        result = 31 * result + kernelDisplayValues.hashCode()
        result = 31 * result + currentX
        result = 31 * result + currentY
        result = 31 * result + (currentCalculation?.hashCode() ?: 0)
        result = 31 * result + progress.hashCode()
        result = 31 * result + processedPixels
        result = 31 * result + totalPixels
        result = 31 * result + isRunning.hashCode()
        result = 31 * result + isPaused.hashCode()
        result = 31 * result + isComplete.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + (autoSpeedConfig?.hashCode() ?: 0)
        result = 31 * result + elapsedTimeMs.hashCode()
        result = 31 * result + blurType.hashCode()
        result = 31 * result + kernelSize.hashCode()
        result = 31 * result + intensity.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + pausedAtX
        result = 31 * result + pausedAtY
        result = 31 * result + (pausedBitmap?.hashCode() ?: 0)
        return result
    }
}

class VisualizationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizationUiState())
    val uiState: StateFlow<VisualizationUiState> = _uiState.asStateFlow()

    private val blurProcessor = BlurProcessor()
    private var processingJob: Job? = null
    private var startTimeMs: Long = 0

    fun initialize(
        context: Context,
        imageUri: String,
        blurType: BlurType,
        kernelSize: KernelSize,
        intensity: BlurIntensity
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    blurType = blurType,
                    kernelSize = kernelSize,
                    intensity = intensity
                )
            }

            try {
                // Check if this is a sample image (passed via ResultHolder)
                val bitmap: Bitmap? = if (imageUri.startsWith("sample://")) {
                    ResultHolder.inputBitmap
                } else {
                    val uri = Uri.parse(imageUri)
                    BitmapUtils.loadAndScaleForVisualization(context, uri)?.first
                }

                if (bitmap != null) {
                    val kernelConfig = KernelConfig(blurType, kernelSize, intensity, intensity.sigma)
                    val kernel = KernelGenerator.generate(kernelConfig)
                    val kernelDisplay = KernelGenerator.formatForDisplay(kernel)

                    val autoSpeedConfig = blurProcessor.calculateAutoSpeed(
                        bitmap.width,
                        bitmap.height,
                        ProcessSpeed.AUTO
                    )

                    _uiState.update {
                        it.copy(
                            originalBitmap = bitmap,
                            displayBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false),
                            kernelMatrix = kernel,
                            kernelDisplayValues = kernelDisplay,
                            totalPixels = bitmap.width * bitmap.height,
                            autoSpeedConfig = autoSpeedConfig,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load image"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun startProcessing() {
        val state = _uiState.value
        val bitmap = state.originalBitmap ?: return
        val autoSpeedConfig = state.autoSpeedConfig ?: return

        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                isComplete = false,
                progress = 0f,
                processedPixels = 0
            )
        }

        startTimeMs = System.currentTimeMillis()

        processingJob = viewModelScope.launch {
            val kernelConfig = KernelConfig(state.blurType, state.kernelSize, state.intensity, state.intensity.sigma)

            blurProcessor.processWithVisualization(bitmap, kernelConfig, autoSpeedConfig)
                .collect { step ->
                    onStepReceived(step)
                }
        }
    }

    private fun onStepReceived(step: BlurStep) {
        val elapsedMs = System.currentTimeMillis() - startTimeMs

        _uiState.update {
            it.copy(
                displayBitmap = step.currentBitmap,
                currentX = step.x,
                currentY = step.y,
                currentCalculation = step.calculation,
                progress = step.progress,
                processedPixels = step.processedPixels,
                elapsedTimeMs = elapsedMs,
                isComplete = step.progress >= 1f,
                isRunning = step.progress < 1f,
                finalBitmap = if (step.progress >= 1f) step.currentBitmap else null
            )
        }
    }

    fun pauseProcessing() {
        processingJob?.cancel()
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = true,
                // Store pause position for resume
                pausedAtX = state.currentX,
                pausedAtY = state.currentY,
                pausedBitmap = state.displayBitmap
            )
        }
    }

    fun resumeProcessing() {
        val state = _uiState.value
        val bitmap = state.originalBitmap ?: return
        val autoSpeedConfig = state.autoSpeedConfig ?: return
        val pausedX = state.pausedAtX
        val pausedY = state.pausedAtY
        val pausedBitmap = state.pausedBitmap

        // Calculate next position (move to next pixel)
        val width = bitmap.width
        var resumeX = pausedX + 1
        var resumeY = pausedY
        if (resumeX >= width) {
            resumeX = 0
            resumeY += 1
        }

        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false
            )
        }

        processingJob = viewModelScope.launch {
            val kernelConfig = KernelConfig(state.blurType, state.kernelSize, state.intensity, state.intensity.sigma)

            blurProcessor.processWithVisualization(
                bitmap = bitmap,
                config = kernelConfig,
                speedConfig = autoSpeedConfig,
                startFromX = resumeX,
                startFromY = resumeY,
                currentOutputBitmap = pausedBitmap
            ).collect { step ->
                onStepReceived(step)
            }
        }
    }

    fun stopProcessing() {
        processingJob?.cancel()
        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isComplete = false,
                progress = 0f,
                processedPixels = 0,
                displayBitmap = it.originalBitmap?.copy(Bitmap.Config.ARGB_8888, false),
                currentCalculation = null
            )
        }
    }

    fun skipToEnd() {
        processingJob?.cancel()

        viewModelScope.launch {
            val state = _uiState.value
            val bitmap = state.originalBitmap ?: return@launch
            val kernelConfig = KernelConfig(state.blurType, state.kernelSize, state.intensity, state.intensity.sigma)

            _uiState.update { it.copy(isLoading = true) }

            val result = blurProcessor.processInstant(bitmap, kernelConfig)

            _uiState.update {
                it.copy(
                    displayBitmap = result,
                    finalBitmap = result,
                    progress = 1f,
                    processedPixels = it.totalPixels,
                    isRunning = false,
                    isPaused = false,
                    isComplete = true,
                    isLoading = false,
                    elapsedTimeMs = System.currentTimeMillis() - startTimeMs
                )
            }
        }
    }

    fun onSpeedChanged(speed: ProcessSpeed) {
        val bitmap = _uiState.value.originalBitmap ?: return
        val autoSpeedConfig = blurProcessor.calculateAutoSpeed(
            bitmap.width,
            bitmap.height,
            speed
        )

        _uiState.update {
            it.copy(
                speed = speed,
                autoSpeedConfig = autoSpeedConfig
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        processingJob?.cancel()
    }
}
