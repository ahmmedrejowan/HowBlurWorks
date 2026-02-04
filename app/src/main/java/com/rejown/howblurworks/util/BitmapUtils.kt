package com.rejown.howblurworks.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

object BitmapUtils {

    private const val MAX_VISUALIZATION_DIMENSION = 400

    /**
     * Load and scale bitmap for visualization
     * Returns scaled bitmap and scale factor
     */
    fun loadAndScaleForVisualization(
        context: Context,
        uri: Uri,
        maxDimension: Int = MAX_VISUALIZATION_DIMENSION
    ): Pair<Bitmap, Float>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            val sampleSize = calculateInSampleSize(options, maxDimension, maxDimension)

            // Decode with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val newInputStream = context.contentResolver.openInputStream(uri) ?: return null
            var bitmap = BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
            newInputStream.close()

            if (bitmap == null) return null

            // Apply rotation if needed
            bitmap = applyExifRotation(context, uri, bitmap)

            // Scale to max dimension
            val scaledResult = scaleToMaxDimension(bitmap, maxDimension)

            scaledResult
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Scale bitmap to fit within max dimension while maintaining aspect ratio
     */
    fun scaleToMaxDimension(bitmap: Bitmap, maxDimension: Int): Pair<Bitmap, Float> {
        val maxDim = maxOf(bitmap.width, bitmap.height)

        if (maxDim <= maxDimension) {
            return bitmap to 1f
        }

        val scale = maxDimension.toFloat() / maxDim
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        return scaledBitmap to scale
    }

    /**
     * Apply EXIF rotation to bitmap
     */
    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotation == 0f) {
                bitmap
            } else {
                val matrix = Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (e: IOException) {
            bitmap
        }
    }

    /**
     * Calculate sample size for efficient loading
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Save bitmap to gallery
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        displayName: String = "HBW_${System.currentTimeMillis()}"
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HBW")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }

        return uri
    }

    /**
     * Format pixel count for display
     */
    fun formatPixelCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
            count >= 1_000 -> String.format("%.1fK", count / 1_000f)
            else -> count.toString()
        }
    }

    /**
     * Format duration for display
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return if (minutes > 0) {
            String.format("%d:%02d", minutes, remainingSeconds)
        } else {
            String.format("%.1fs", durationMs / 1000f)
        }
    }
}
