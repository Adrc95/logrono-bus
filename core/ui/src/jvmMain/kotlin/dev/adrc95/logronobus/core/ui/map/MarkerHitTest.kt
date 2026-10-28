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
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel

internal fun busAt(
    position: Offset,
    buses: List<BusLocationDisplayModel>,
    projected: (BusLocationDisplayModel) -> Offset,
    radiusPx: Float,
): BusLocationDisplayModel? {
    val limit = radiusPx * radiusPx
    var best: BusLocationDisplayModel? = null
    var bestDistance = Float.MAX_VALUE
    for (bus in buses) {
        val delta = projected(bus) - position
        val distance = delta.x * delta.x + delta.y * delta.y
        if (distance < limit && distance < bestDistance) {
            best = bus
            bestDistance = distance
        }
    }
    return best
}

internal fun stopAt(
    position: Offset,
    stops: List<StopDisplayModel>,
    projected: (StopDisplayModel) -> Offset,
    radiusPx: (StopDisplayModel) -> Float,
    isVisible: (StopDisplayModel) -> Boolean,
): StopDisplayModel? {
    var best: StopDisplayModel? = null
    var bestDistance = Float.MAX_VALUE
    for (stop in stops) {
        if (!isVisible(stop)) continue
        val delta = projected(stop) - position
        val distance = delta.x * delta.x + delta.y * delta.y
        val limit = radiusPx(stop)
        if (distance < limit * limit && distance < bestDistance) {
            best = stop
            bestDistance = distance
        }
    }
    return best
}
