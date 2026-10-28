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

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawBusMarker(
    center: Offset,
    color: Color,
    headingDegrees: Float?,
    isTracked: Boolean,
    haloAlpha: Float,
    haloRadius: Float,
    isReturnDirection: Boolean,
    label: TextLayoutResult?,
    glyph: VectorPainter,
) {
    if (isTracked) {
        drawCircle(
            TRACKED_HALO_COLOR.copy(alpha = haloAlpha),
            radius = haloRadius,
            center = center,
        )
    }

    headingDegrees?.let { degrees ->
        fun nose(
            dx: Float,
            dy: Float,
        ): Path {
            val radians = degrees * (PI / 180f).toFloat()
            val fx = sin(radians)
            val fy = -cos(radians)
            val tipX = center.x + dx + fx * (BUS_MARKER_RADIUS + BUS_NOSE_LENGTH)
            val tipY = center.y + dy + fy * (BUS_MARKER_RADIUS + BUS_NOSE_LENGTH)
            val baseX = center.x + dx + fx * (BUS_MARKER_RADIUS - BUS_NOSE_BASE)
            val baseY = center.y + dy + fy * (BUS_MARKER_RADIUS - BUS_NOSE_BASE)
            val sideX = -fy * BUS_NOSE_HALF_WIDTH
            val sideY = fx * BUS_NOSE_HALF_WIDTH
            return Path().apply {
                moveTo(tipX, tipY)
                lineTo(baseX + sideX, baseY + sideY)
                lineTo(baseX - sideX, baseY - sideY)
                close()
            }
        }
        drawPath(nose(BUS_MARKER_SHADOW_OFFSET, BUS_MARKER_SHADOW_OFFSET), MARKER_SHADOW_COLOR)
        drawPath(nose(0f, 0f), color)
    }

    if (isReturnDirection) {
        val side = Size(BUS_MARKER_RADIUS * 2, BUS_MARKER_RADIUS * 2)
        val corner = CornerRadius(BUS_SQUARE_CORNER, BUS_SQUARE_CORNER)
        drawRoundRect(
            MARKER_SHADOW_COLOR,
            topLeft =
                Offset(
                    center.x - BUS_MARKER_RADIUS + BUS_MARKER_SHADOW_OFFSET,
                    center.y - BUS_MARKER_RADIUS + BUS_MARKER_SHADOW_OFFSET,
                ),
            size = side,
            cornerRadius = corner,
        )
        drawRoundRect(
            color,
            topLeft = Offset(center.x - BUS_MARKER_RADIUS, center.y - BUS_MARKER_RADIUS),
            size = side,
            cornerRadius = corner,
        )
    } else {
        drawCircle(
            MARKER_SHADOW_COLOR,
            radius = BUS_MARKER_RADIUS + BUS_MARKER_SHADOW_OFFSET,
            center =
                Offset(
                    center.x + BUS_MARKER_SHADOW_OFFSET,
                    center.y + BUS_MARKER_SHADOW_OFFSET,
                ),
        )
        drawCircle(color, radius = BUS_MARKER_RADIUS, center = center)
    }

    val tint =
        if (color.luminance() > BUS_LUMINANCE_THRESHOLD) GLYPH_TINT_ON_LIGHT else Color.White
    if (label != null) {
        drawText(
            label,
            color = tint,
            topLeft =
                Offset(
                    center.x - label.size.width / 2f,
                    center.y - label.size.height / 2f,
                ),
        )
    } else {
        translate(center.x - BUS_GLYPH_SIZE / 2, center.y - BUS_GLYPH_SIZE / 2) {
            with(glyph) {
                draw(
                    size = Size(BUS_GLYPH_SIZE, BUS_GLYPH_SIZE),
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }
    }
}
