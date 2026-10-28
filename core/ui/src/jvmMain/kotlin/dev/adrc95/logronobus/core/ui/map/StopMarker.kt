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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText

internal fun DrawScope.drawStopMarker(
    center: Offset,
    isTracked: Boolean,
    isPastEta: Boolean,
    label: TextLayoutResult?,
) {
    val radius = if (isTracked) TRACKED_STOP_RADIUS else STOP_MARKER_RADIUS
    val dotColor =
        when {
            isTracked -> TRACKED_HALO_COLOR
            isPastEta -> STOP_PAST_COLOR
            else -> STOP_DEFAULT_COLOR
        }
    drawCircle(dotColor, radius = radius, center = center)
    drawCircle(
        Color.White,
        radius = radius,
        center = center,
        style = Stroke(width = STOP_MARKER_BORDER),
    )
    label ?: return

    val left = center.x + radius + ETA_LABEL_GAP
    val top = center.y - label.size.height / 2f
    drawRoundRect(
        color = Color.White.copy(alpha = ETA_BOX_ALPHA),
        topLeft = Offset(left - ETA_BOX_PADDING_X, top - ETA_BOX_PADDING_Y),
        size =
            Size(
                label.size.width + ETA_BOX_PADDING_X * 2,
                label.size.height + ETA_BOX_PADDING_Y * 2,
            ),
        cornerRadius = CornerRadius(ETA_BOX_CORNER, ETA_BOX_CORNER),
    )
    drawText(
        label,
        color = if (isPastEta) ETA_TEXT_PAST_COLOR else ETA_TEXT_COLOR,
        topLeft = Offset(left, top),
    )
}
