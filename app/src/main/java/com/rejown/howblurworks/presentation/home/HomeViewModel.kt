package com.rejown.howblurworks.presentation.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.howblurworks.domain.engine.BlurProcessor
import com.rejown.howblurworks.domain.engine.KernelGenerator
import com.rejown.howblurworks.domain.model.BlurTheoryItem
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelConfig
import com.rejown.howblurworks.domain.model.KernelPreview
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.ProcessSpeed
import com.rejown.howblurworks.domain.model.SampleImage
import com.rejown.howblurworks.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val error: String? = null,
    // New fields for redesigned home screen
    val sampleImages: List<SampleImage> = emptyList(),
    val theoryItems: List<BlurTheoryItem> = emptyList(),
    val selectedTheoryItem: BlurTheoryItem? = null,
    val showTheorySheet: Boolean = false,
    val kernelPreview: KernelPreview? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val blurProcessor = BlurProcessor()

    init {
        // Initialize theory items
        _uiState.update { it.copy(theoryItems = getTheoryItems()) }
        // Initialize kernel preview
        updateKernelPreview()
    }

    private fun getTheoryItems(): List<BlurTheoryItem> = listOf(
        BlurTheoryItem(
            id = "what_is_blur",
            title = "What is Blur?",
            subtitle = "The basics of image blurring",
            icon = "blur_on",
            content = "Image blur is achieved by averaging nearby pixel values. Each pixel's new color becomes a weighted combination of its neighbors, creating a smoothing effect that reduces detail and sharp edges.",
            bulletPoints = listOf(
                "Blur reduces high-frequency details",
                "Each pixel is replaced by an average of its neighbors",
                "Larger blur radius = more smoothing",
                "Used for noise reduction, depth effects, and privacy"
            )
        ),
        BlurTheoryItem(
            id = "convolution",
            title = "Convolution",
            subtitle = "How kernels slide over images",
            icon = "grid_on",
            content = "Convolution is the mathematical operation that applies blur. A small matrix called a 'kernel' slides over every pixel, multiplying neighboring values by weights and summing the results.",
            bulletPoints = listOf(
                "Kernel is a matrix of weights (e.g., 3×3, 5×5)",
                "Kernel center is placed on each pixel",
                "Neighbors are multiplied by kernel weights",
                "Sum of weighted values becomes new pixel color"
            )
        ),
        BlurTheoryItem(
            id = "blur_types",
            title = "Blur Types",
            subtitle = "Different algorithms explained",
            icon = "category",
            content = "Different blur algorithms use different kernel patterns to achieve various effects.",
            bulletPoints = listOf(
                "Gaussian: Bell-curve weights, natural looking blur",
                "Box: Equal weights, faster but can show artifacts",
                "Motion Horizontal: Only horizontal neighbors, simulates camera shake",
                "Motion Vertical: Only vertical neighbors, simulates vertical movement"
            )
        ),
        BlurTheoryItem(
            id = "kernel_size",
            title = "Kernel Size",
            subtitle = "Effect of 3×3 vs 5×5 vs 7×7",
            icon = "apps",
            content = "Kernel size determines how many neighboring pixels influence each result. Larger kernels create stronger blur but require more computation.",
            bulletPoints = listOf(
                "3×3: 9 pixels sampled - subtle blur, fastest",
                "5×5: 25 pixels sampled - moderate blur",
                "7×7: 49 pixels sampled - strong blur, slowest",
                "Larger kernel = more neighbors = smoother result"
            )
        )
    )

    fun initializeSampleImages(context: Context, sampleImageResIds: List<Pair<String, Int>>) {
        val samples = sampleImageResIds.mapIndexed { index, (name, resId) ->
            val descriptions = listOf("Landscape scene", "Portrait photo", "Urban architecture", "Natural flora")
            SampleImage(
                id = "sample_$index",
                name = name,
                drawableResId = resId,
                description = descriptions.getOrElse(index) { "Sample image" }
            )
        }
        _uiState.update { it.copy(sampleImages = samples) }
    }

    fun onSampleImageSelected(context: Context, sampleImage: SampleImage) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeResource(context.resources, sampleImage.drawableResId)
                }
                if (bitmap != null) {
                    val (scaledBitmap, scale) = BitmapUtils.scaleToMaxDimension(bitmap, 400)
                    val autoSpeed = blurProcessor.calculateAutoSpeed(
                        scaledBitmap.width,
                        scaledBitmap.height,
                        _uiState.value.selectedSpeed
                    )
                    _uiState.update {
                        it.copy(
                            selectedImageUri = "sample://${sampleImage.id}",
                            selectedBitmap = scaledBitmap,
                            imageWidth = scaledBitmap.width,
                            imageHeight = scaledBitmap.height,
                            scaleFactor = scale,
                            estimatedDuration = autoSpeed.estimatedDurationSec,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load sample image") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun onTheoryItemClick(item: BlurTheoryItem) {
        _uiState.update { it.copy(selectedTheoryItem = item, showTheorySheet = true) }
    }

    fun dismissTheorySheet() {
        _uiState.update { it.copy(showTheorySheet = false) }
    }

    private fun updateKernelPreview() {
        val state = _uiState.value
        val config = KernelConfig(type = state.selectedBlurType, size = state.selectedKernelSize)
        val kernel = KernelGenerator.generate(config)
        val formatted = KernelGenerator.formatForDisplay(kernel)
        val description = getKernelDescription(state.selectedBlurType, state.selectedKernelSize)
        _uiState.update {
            it.copy(kernelPreview = KernelPreview(formatted, description, state.selectedBlurType, state.selectedKernelSize))
        }
    }

    private fun getKernelDescription(blurType: BlurType, kernelSize: KernelSize): String {
        val sizeDesc = when (kernelSize) {
            KernelSize.SMALL -> "3×3 kernel samples 9 pixels"
            KernelSize.MEDIUM -> "5×5 kernel samples 25 pixels"
            KernelSize.LARGE -> "7×7 kernel samples 49 pixels"
        }
        val typeDesc = when (blurType) {
            BlurType.GAUSSIAN -> "Gaussian weights: center pixels have more influence"
            BlurType.BOX -> "Box weights: all pixels contribute equally"
            BlurType.MOTION_HORIZONTAL -> "Motion blur: only horizontal neighbors used"
            BlurType.MOTION_VERTICAL -> "Motion blur: only vertical neighbors used"
        }
        return "$sizeDesc\n$typeDesc"
    }

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
        updateKernelPreview()
    }

    fun onKernelSizeSelected(kernelSize: KernelSize) {
        _uiState.update { it.copy(selectedKernelSize = kernelSize) }
        updateKernelPreview()
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
