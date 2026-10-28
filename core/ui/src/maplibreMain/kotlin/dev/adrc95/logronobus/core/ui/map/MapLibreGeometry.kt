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

import dev.adrc95.logronobus.core.model.GeoPoint
import org.maplibre.spatialk.geojson.BoundingBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

internal fun List<GeoPoint>.boundingBox(): BoundingBox =
    BoundingBox(
        west = minOf { it.lon } - 0.004,
        south = minOf { it.lat } - 0.004,
        east = maxOf { it.lon } + 0.004,
        north = maxOf { it.lat } + 0.004,
    )

internal fun metres(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    val dLat = (lat2 - lat1) * 111_320.0
    val dLon = (lon2 - lon1) * 111_320.0 * cos((lat1 + lat2) / 2 * PI / 180)
    return sqrt(dLat * dLat + dLon * dLon)
}
