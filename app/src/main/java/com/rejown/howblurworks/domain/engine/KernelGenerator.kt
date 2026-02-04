package com.rejown.howblurworks.domain.engine

import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelConfig
import kotlin.math.exp

/**
 * Generates convolution kernels for different blur algorithms
 */
object KernelGenerator {

    /**
     * Generate kernel matrix based on configuration
     */
    fun generate(config: KernelConfig): Array<FloatArray> {
        return when (config.type) {
            BlurType.GAUSSIAN -> gaussian(config.size.size, config.sigma)
            BlurType.BOX -> box(config.size.size)
            BlurType.MOTION_HORIZONTAL -> motionHorizontal(config.size.size)
            BlurType.MOTION_VERTICAL -> motionVertical(config.size.size)
        }
    }

    /**
     * Generate Gaussian blur kernel
     * Uses 2D Gaussian function: G(x,y) = (1/2πσ²) * e^(-(x²+y²)/2σ²)
     */
    fun gaussian(size: Int, sigma: Float): Array<FloatArray> {
        val kernel = Array(size) { FloatArray(size) }
        val radius = size / 2
        var sum = 0.0

        for (y in -radius..radius) {
            for (x in -radius..radius) {
                val exponent = -(x * x + y * y) / (2.0 * sigma * sigma)
                val value = exp(exponent)
                kernel[y + radius][x + radius] = value.toFloat()
                sum += value
            }
        }

        // Normalize so all weights sum to 1
        for (y in 0 until size) {
            for (x in 0 until size) {
                kernel[y][x] /= sum.toFloat()
            }
        }

        return kernel
    }

    /**
     * Generate Box blur kernel (equal weights)
     */
    fun box(size: Int): Array<FloatArray> {
        val value = 1f / (size * size)
        return Array(size) { FloatArray(size) { value } }
    }

    /**
     * Generate horizontal motion blur kernel
     */
    fun motionHorizontal(size: Int): Array<FloatArray> {
        val kernel = Array(size) { FloatArray(size) { 0f } }
        val center = size / 2
        val value = 1f / size

        for (x in 0 until size) {
            kernel[center][x] = value
        }

        return kernel
    }

    /**
     * Generate vertical motion blur kernel
     */
    fun motionVertical(size: Int): Array<FloatArray> {
        val kernel = Array(size) { FloatArray(size) { 0f } }
        val center = size / 2
        val value = 1f / size

        for (y in 0 until size) {
            kernel[y][center] = value
        }

        return kernel
    }

    /**
     * Format kernel for display (rounded values)
     */
    fun formatForDisplay(kernel: Array<FloatArray>): List<List<String>> {
        return kernel.map { row ->
            row.map { value ->
                String.format("%.2f", value)
            }
        }
    }
}
