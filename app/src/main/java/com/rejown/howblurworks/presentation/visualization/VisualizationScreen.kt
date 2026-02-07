package com.rejown.howblurworks.presentation.visualization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rejown.howblurworks.data.ResultHolder
import com.rejown.howblurworks.domain.model.BlurIntensity
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.PixelCalculation
import com.rejown.howblurworks.domain.model.ProcessSpeed
import com.rejown.howblurworks.util.BitmapUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizationScreen(
    imageUri: String,
    blurType: BlurType,
    kernelSize: KernelSize,
    intensity: BlurIntensity,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    viewModel: VisualizationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(imageUri) {
        viewModel.initialize(context, imageUri, blurType, kernelSize, intensity)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Blur Visualization",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Text(
                        text = when {
                            uiState.isComplete -> "Done"
                            uiState.isRunning -> "Processing"
                            uiState.isPaused -> "Paused"
                            else -> "Ready"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.originalBitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Image Display with Cursor Overlay
                ImageDisplayCard(
                    bitmap = uiState.displayBitmap,
                    imageWidth = uiState.displayBitmap?.width ?: 0,
                    imageHeight = uiState.displayBitmap?.height ?: 0,
                    currentX = uiState.currentX,
                    currentY = uiState.currentY,
                    kernelSize = uiState.kernelSize.size,
                    isRunning = uiState.isRunning,
                    progress = uiState.progress
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kernel and Pixel Info - side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KernelDisplayCard(
                        kernelValues = uiState.kernelDisplayValues,
                        blurType = uiState.blurType,
                        kernelSize = uiState.kernelSize,
                        modifier = Modifier.weight(1f)
                    )

                    PixelInfoCard(
                        calculation = uiState.currentCalculation,
                        currentX = uiState.currentX,
                        currentY = uiState.currentY,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress
                ProgressSection(
                    progress = uiState.progress,
                    processedPixels = uiState.processedPixels,
                    totalPixels = uiState.totalPixels,
                    elapsedTimeMs = uiState.elapsedTimeMs
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Speed Selector
                if (!uiState.isComplete) {
                    SpeedSelector(
                        selectedSpeed = uiState.speed,
                        onSpeedSelected = { viewModel.onSpeedChanged(it) },
                        enabled = !uiState.isRunning
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controls
                ControlsSection(
                    isRunning = uiState.isRunning,
                    isPaused = uiState.isPaused,
                    isComplete = uiState.isComplete,
                    onStart = { viewModel.startProcessing() },
                    onPause = { viewModel.pauseProcessing() },
                    onResume = { viewModel.resumeProcessing() },
                    onStop = { viewModel.stopProcessing() },
                    onSkip = { viewModel.skipToEnd() }
                )

                // Show "View Result" button when complete
                if (uiState.isComplete) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Save data to ResultHolder before navigating
                            ResultHolder.setResult(
                                original = uiState.originalBitmap,
                                blurred = uiState.finalBitmap ?: uiState.displayBitmap,
                                type = uiState.blurType,
                                size = uiState.kernelSize,
                                width = uiState.originalBitmap?.width ?: 0,
                                height = uiState.originalBitmap?.height ?: 0,
                                pixels = uiState.processedPixels,
                                timeMs = uiState.elapsedTimeMs
                            )
                            onNavigateToResult()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("View Result")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageDisplayCard(
    bitmap: android.graphics.Bitmap?,
    imageWidth: Int,
    imageHeight: Int,
    currentX: Int,
    currentY: Int,
    kernelSize: Int,
    isRunning: Boolean,
    progress: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val processedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { bmp ->
                // Calculate image display dimensions
                val containerWidth = constraints.maxWidth.toFloat()
                val containerHeight = constraints.maxHeight.toFloat()
                val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
                val containerAspect = containerWidth / containerHeight

                val (displayWidth, displayHeight) = if (imageAspect > containerAspect) {
                    containerWidth to (containerWidth / imageAspect)
                } else {
                    (containerHeight * imageAspect) to containerHeight
                }

                val offsetX = (containerWidth - displayWidth) / 2
                val offsetY = (containerHeight - displayHeight) / 2

                // Scale factors
                val scaleX = displayWidth / imageWidth
                val scaleY = displayHeight / imageHeight

                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Processing image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )

                // Draw cursor overlay
                if (isRunning && imageWidth > 0 && imageHeight > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val kernelRadius = kernelSize / 2
                        val cursorSize = kernelSize * scaleX

                        // Calculate cursor position
                        val cursorX = offsetX + (currentX * scaleX) - (cursorSize / 2)
                        val cursorY = offsetY + (currentY * scaleY) - (cursorSize / 2)

                        // Draw progress line (horizontal scanline effect)
                        val progressY = offsetY + (currentY * scaleY)
                        drawLine(
                            color = processedColor,
                            start = Offset(offsetX, progressY),
                            end = Offset(offsetX + displayWidth, progressY),
                            strokeWidth = 2f
                        )

                        // Draw kernel cursor rectangle
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(cursorX, cursorY),
                            size = Size(cursorSize, cursorSize),
                            style = Stroke(width = 3f)
                        )

                        // Draw center pixel highlight
                        val centerSize = scaleX.coerceAtLeast(4f)
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(
                                offsetX + (currentX * scaleX) - centerSize / 2,
                                offsetY + (currentY * scaleY) - centerSize / 2
                            ),
                            size = Size(centerSize, centerSize)
                        )

                        // Draw processed area overlay (semi-transparent)
                        if (progress > 0 && progress < 1) {
                            val processedHeight = currentY * scaleY
                            drawRect(
                                color = processedColor.copy(alpha = 0.1f),
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(displayWidth, processedHeight)
                            )
                        }
                    }
                }

                // Position indicator label
                if (isRunning) {
                    Text(
                        text = "($currentX, $currentY)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KernelDisplayCard(
    kernelValues: List<List<String>>,
    blurType: BlurType,
    kernelSize: KernelSize,
    modifier: Modifier = Modifier
) {
    val cardHeight = 130.dp

    Card(
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kernel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = kernelSize.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Kernel matrix display - centered
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Compact cell sizes for side-by-side layout
                    val cellSize = when (kernelValues.size) {
                        3 -> 20.dp
                        5 -> 14.dp
                        7 -> 10.dp
                        9 -> 8.dp
                        else -> 7.dp
                    }

                    kernelValues.forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            row.forEachIndexed { colIndex, value ->
                                val isCenter = rowIndex == kernelValues.size / 2 &&
                                        colIndex == row.size / 2
                                val valueFloat = value.toFloatOrNull() ?: 0f
                                val isActive = valueFloat > 0.001f

                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .background(
                                            when {
                                                isCenter -> MaterialTheme.colorScheme.primary
                                                isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                            },
                                            RoundedCornerShape(2.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Only highlight center for visual reference
                                }
                            }
                        }
                    }
                }
            }

            // Footer
            Text(
                text = blurType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PixelInfoCard(
    calculation: PixelCalculation?,
    currentX: Int,
    currentY: Int,
    modifier: Modifier = Modifier
) {
    val cardHeight = 130.dp

    Card(
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pixel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (calculation != null) "($currentX, $currentY)" else "--",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Color transform display - centered
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (calculation != null) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Original color
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Color(calculation.centerPixel.color),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "In",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "→",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Result color
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Color(calculation.resultColor),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Waiting...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Footer
            Text(
                text = "Color Transform",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressSection(
    progress: Float,
    processedPixels: Int,
    totalPixels: Int,
    elapsedTimeMs: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pixels",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${BitmapUtils.formatPixelCount(processedPixels)} / ${BitmapUtils.formatPixelCount(totalPixels)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Elapsed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = BitmapUtils.formatDuration(elapsedTimeMs),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsSection(
    isRunning: Boolean,
    isPaused: Boolean,
    isComplete: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            isComplete -> {
                // No controls needed when complete
            }
            isRunning -> {
                // Compact buttons for running state - icons with short labels
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = "Skip",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip", style = MaterialTheme.typography.labelMedium)
                }
            }
            isPaused -> {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume")
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }
            }
            else -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Visualization")
                }
            }
        }
    }
}

@Composable
private fun SpeedSelector(
    selectedSpeed: ProcessSpeed,
    onSpeedSelected: (ProcessSpeed) -> Unit,
    enabled: Boolean
) {
    val speedDescriptions = mapOf(
        ProcessSpeed.SLOW to "60 seconds • Best for learning, see every detail",
        ProcessSpeed.AUTO to "30 seconds • Balanced speed and visibility",
        ProcessSpeed.FAST to "15 seconds • Quick overview of the process",
        ProcessSpeed.INSTANT to "Immediate • Skip animation, see result only"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Processing Speed",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Speed options in a 2x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProcessSpeed.entries.take(2).forEach { speed ->
                        SpeedOptionChip(
                            speed = speed,
                            isSelected = speed == selectedSpeed,
                            enabled = enabled,
                            onClick = { onSpeedSelected(speed) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProcessSpeed.entries.drop(2).forEach { speed ->
                        SpeedOptionChip(
                            speed = speed,
                            isSelected = speed == selectedSpeed,
                            enabled = enabled,
                            onClick = { onSpeedSelected(speed) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info text for selected speed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = speedDescriptions[selectedSpeed] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeedOptionChip(
    speed: ProcessSpeed,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (speed) {
        ProcessSpeed.SLOW -> Icons.Default.PlayArrow
        ProcessSpeed.AUTO -> Icons.Default.PlayArrow
        ProcessSpeed.FAST -> Icons.Default.FastForward
        ProcessSpeed.INSTANT -> Icons.Default.FastForward
    }

    Card(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (enabled) Modifier.border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = { if (enabled) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = speed.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
