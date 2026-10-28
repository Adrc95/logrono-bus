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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate as drawRotate

internal fun DrawScope.drawRouteLine(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    val path = Path()
    points.forEachIndexed { index, at ->
        if (index == 0) path.moveTo(at.x, at.y) else path.lineTo(at.x, at.y)
    }
    drawPath(
        path,
        Color.White.copy(alpha = ROUTE_CASING_ALPHA),
        style = Stroke(width = ROUTE_CASING_WIDTH),
    )
    drawPath(path, color, style = Stroke(width = ROUTE_LINE_WIDTH))

    val arrow = directionArrow(points) ?: return
    drawRotate(degrees = arrow.degrees, pivot = arrow.center) {
        drawPath(
            arrowHead(arrow.center, ARROW_OUTER_TIP, ARROW_OUTER_BASE, ARROW_OUTER_HALF_WIDTH),
            Color.White,
        )
        drawPath(
            arrowHead(arrow.center, ARROW_INNER_TIP, ARROW_INNER_BASE, ARROW_INNER_HALF_WIDTH),
            color,
        )
    }
}

private fun arrowHead(
    center: Offset,
    tip: Float,
    base: Float,
    halfWidth: Float,
): Path =
    Path().apply {
        moveTo(center.x + tip, center.y)
        lineTo(center.x + base, center.y - halfWidth)
        lineTo(center.x + base, center.y + halfWidth)
        close()
    }
