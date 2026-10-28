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
import kotlin.math.PI
import kotlin.math.atan2

internal data class DirectionArrow(
    val center: Offset,
    val degrees: Float,
)

internal fun directionArrow(points: List<Offset>): DirectionArrow? {
    if (points.size < 2) return null
    var total = 0f
    for (i in 1 until points.size) total += (points[i] - points[i - 1]).getDistance()
    if (total <= 0f) return null

    var target = total / 2f
    for (i in 1 until points.size) {
        val from = points[i - 1]
        val to = points[i]
        val length = (to - from).getDistance()
        if (length <= 0f) continue
        if (target <= length) {
            val at = from + (to - from) * (target / length)
            val degrees = atan2(to.y - from.y, to.x - from.x) * 180.0 / PI
            return DirectionArrow(center = at, degrees = degrees.toFloat())
        }
        target -= length
    }
    return null
}
