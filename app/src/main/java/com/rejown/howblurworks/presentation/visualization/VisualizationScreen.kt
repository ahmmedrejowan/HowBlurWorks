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
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.PixelCalculation
import com.rejown.howblurworks.util.BitmapUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizationScreen(
    imageUri: String,
    blurType: BlurType,
    kernelSize: KernelSize,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    viewModel: VisualizationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(imageUri) {
        viewModel.initialize(context, imageUri, blurType, kernelSize)
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

                // Kernel and Pixel Info
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

                Spacer(modifier = Modifier.height(24.dp))

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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KERNEL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kernel matrix display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                kernelValues.forEachIndexed { rowIndex, row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        row.forEachIndexed { colIndex, value ->
                            val isCenter = rowIndex == kernelValues.size / 2 &&
                                    colIndex == row.size / 2
                            Box(
                                modifier = Modifier
                                    .size(if (kernelValues.size <= 3) 32.dp else 24.dp)
                                    .background(
                                        if (isCenter) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        if (isCenter) 2.dp else 1.dp,
                                        if (isCenter) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = if (kernelValues.size <= 3) 9.sp else 7.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isCenter) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${blurType.displayName} ${kernelSize.displayName}",
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PIXEL INFO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (calculation != null) {
                Text(
                    text = "Pos: ($currentX, $currentY)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Original color
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "IN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(calculation.centerPixel.color),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Result color
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(calculation.resultColor),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            } else {
                Text(
                    text = "Position: --",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Waiting...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${BitmapUtils.formatPixelCount(processedPixels)} / ${BitmapUtils.formatPixelCount(totalPixels)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = BitmapUtils.formatDuration(elapsedTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isComplete -> {
                // No controls needed when complete
            }
            isRunning -> {
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause")
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }

                FilledTonalButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Skip")
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
                    Text("Start")
                }
            }
        }
    }
}
