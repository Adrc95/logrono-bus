/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.adrc95.logronobus.feature.scan

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetBytesPerRow
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetPixelFormatType
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import kotlin.math.abs

@OptIn(ExperimentalForeignApi::class)
internal class PixelFrame private constructor(
    private val base: CPointer<ByteVar>,
    private val rowBytes: Int,
    private val width: Int,
    private val height: Int,
) {
    fun inkColour(
        left: Int,
        top: Int,
        width: Int,
        height: Int,
    ): Int {
        val insetX = (width * ScanConstants.INSET).toInt()
        val insetY = (height * ScanConstants.INSET_Y).toInt()
        val from = (left + insetX).coerceIn(0, this.width - 1)
        val to = (left + width - insetX).coerceIn(from + 1, this.width)
        val upper = (top + insetY).coerceIn(0, this.height - 1)
        val lower = (top + height - insetY).coerceIn(upper + 1, this.height)
        if (to - from < 2 || lower - upper < 2) return 0
        val counts = HashMap<Int, Int>()
        val pixels = ArrayList<Int>()
        for (y in upper until lower) {
            val row = y * rowBytes
            for (x in from until to) {
                val offset = row + x * BYTES_PER_PIXEL
                val blue = base[offset].toInt() and 0xFF
                val green = base[offset + 1].toInt() and 0xFF
                val red = base[offset + 2].toInt() and 0xFF
                val key = (red shr 4 shl 8) or (green shr 4 shl 4) or (blue shr 4)
                counts[key] = (counts[key] ?: 0) + 1
                pixels += (red shl 16) or (green shl 8) or blue
            }
        }
        val background = counts.maxByOrNull { it.value }?.key ?: return 0
        val backgroundRed = (background shr 8 and 0xF) * 17
        val backgroundGreen = (background shr 4 and 0xF) * 17
        val backgroundBlue = (background and 0xF) * 17
        val ink = ArrayList<Int>()
        pixels.forEach { pixel ->
            val red = pixel shr 16 and 0xFF
            val green = pixel shr 8 and 0xFF
            val blue = pixel and 0xFF
            val distance =
                abs(red - backgroundRed) +
                    abs(green - backgroundGreen) +
                    abs(blue - backgroundBlue)
            if (distance > ScanConstants.INK_DISTANCE) ink += maxOf(red, green, blue) - minOf(red, green, blue)
        }
        if (ink.size < ScanConstants.MIN_INK) return 0
        ink.sort()
        return ink[ink.size * ScanConstants.COLOUR_PERCENTILE / 100]
    }

    companion object {
        private const val BYTES_PER_PIXEL = 4

        fun of(buffer: CVPixelBufferRef): PixelFrame? {
            if (CVPixelBufferGetPixelFormatType(buffer) != kCVPixelFormatType_32BGRA) return null
            val base = CVPixelBufferGetBaseAddress(buffer)?.reinterpret<ByteVar>() ?: return null
            return PixelFrame(
                base = base,
                rowBytes = CVPixelBufferGetBytesPerRow(buffer).toInt(),
                width = CVPixelBufferGetWidth(buffer).toInt(),
                height = CVPixelBufferGetHeight(buffer).toInt(),
            )
        }
    }
}
