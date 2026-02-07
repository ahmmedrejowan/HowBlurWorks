package com.rejown.howblurworks.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ImageComparisonSlider(
    originalBitmap: Bitmap?,
    blurredBitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()

        if (originalBitmap != null && blurredBitmap != null) {
            // Calculate image dimensions to fit in container (ContentScale.Fit)
            val imageAspect = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val containerAspect = maxWidthPx / maxHeightPx

            val (drawWidth, drawHeight) = if (imageAspect > containerAspect) {
                maxWidthPx to (maxWidthPx / imageAspect)
            } else {
                (maxHeightPx * imageAspect) to maxHeightPx
            }

            val offsetX = (maxWidthPx - drawWidth) / 2
            val offsetY = (maxHeightPx - drawHeight) / 2

            // Draw both images using Canvas with proper clipping
            Canvas(modifier = Modifier.fillMaxSize()) {
                val originalImageBitmap = originalBitmap.asImageBitmap()
                val blurredImageBitmap = blurredBitmap.asImageBitmap()

                val dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt())
                val dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())

                // Draw blurred image (full)
                drawImage(
                    image = blurredImageBitmap,
                    dstOffset = dstOffset,
                    dstSize = dstSize
                )

                // Clip and draw original image (left side based on slider)
                val clipX = offsetX + (drawWidth * sliderPosition)
                val clipPath = Path().apply {
                    addRect(Rect(0f, 0f, clipX, maxHeightPx))
                }

                clipPath(clipPath) {
                    drawImage(
                        image = originalImageBitmap,
                        dstOffset = dstOffset,
                        dstSize = dstSize
                    )
                }
            }

            // Slider divider line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset {
                        IntOffset(
                            x = (offsetX + drawWidth * sliderPosition - 1.5f).roundToInt(),
                            y = 0
                        )
                    }
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Slider handle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .offset {
                        IntOffset(
                            x = (offsetX + drawWidth * sliderPosition - 20.dp.toPx()).roundToInt(),
                            y = (maxHeightPx / 2 - 20.dp.toPx()).roundToInt()
                        )
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            val newPosition = sliderPosition + (dragAmount / drawWidth)
                            sliderPosition = newPosition.coerceIn(0.05f, 0.95f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = (-4).dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 4.dp)
                )
            }

            // Labels
            Text(
                text = "Original",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Text(
                text = "Blurred",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else {
            // Placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No images available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
