package com.rejown.howblurworks.presentation.home

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.howblurworks.domain.engine.BlurProcessor
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.ProcessSpeed
import com.rejown.howblurworks.util.BitmapUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedImageUri: String? = null,
    val selectedBitmap: Bitmap? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val scaleFactor: Float = 1f,
    val selectedBlurType: BlurType = BlurType.GAUSSIAN,
    val selectedKernelSize: KernelSize = KernelSize.SMALL,
    val selectedSpeed: ProcessSpeed = ProcessSpeed.AUTO,
    val estimatedDuration: Int = 30,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val blurProcessor = BlurProcessor()

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = BitmapUtils.loadAndScaleForVisualization(context, uri)
                if (result != null) {
                    val (bitmap, scale) = result
                    val autoSpeed = blurProcessor.calculateAutoSpeed(
                        bitmap.width,
                        bitmap.height,
                        _uiState.value.selectedSpeed
                    )

                    _uiState.update {
                        it.copy(
                            selectedImageUri = uri.toString(),
                            selectedBitmap = bitmap,
                            imageWidth = bitmap.width,
                            imageHeight = bitmap.height,
                            scaleFactor = scale,
                            estimatedDuration = autoSpeed.estimatedDurationSec,
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

    fun onBlurTypeSelected(blurType: BlurType) {
        _uiState.update { it.copy(selectedBlurType = blurType) }
    }

    fun onKernelSizeSelected(kernelSize: KernelSize) {
        _uiState.update { it.copy(selectedKernelSize = kernelSize) }
    }

    fun onSpeedSelected(speed: ProcessSpeed) {
        val bitmap = _uiState.value.selectedBitmap
        val estimatedDuration = if (bitmap != null) {
            val autoSpeed = blurProcessor.calculateAutoSpeed(
                bitmap.width,
                bitmap.height,
                speed
            )
            autoSpeed.estimatedDurationSec
        } else {
            speed.targetDurationSec
        }

        _uiState.update {
            it.copy(
                selectedSpeed = speed,
                estimatedDuration = estimatedDuration
            )
        }
    }

    fun clearImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                selectedBitmap = null,
                imageWidth = 0,
                imageHeight = 0,
                scaleFactor = 1f
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
