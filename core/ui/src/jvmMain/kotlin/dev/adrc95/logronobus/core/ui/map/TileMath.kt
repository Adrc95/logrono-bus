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

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

fun ancestorCrop(
    tileX: Int,
    tileY: Int,
    levels: Int,
    ancestorWidth: Int,
): TileCrop {
    val factor = 1 shl levels
    val cell = ancestorWidth / factor
    return TileCrop(
        offsetX = (tileX and (factor - 1)) * cell,
        offsetY = (tileY and (factor - 1)) * cell,
        size = cell,
    )
}

internal fun lonToTileXf(
    lon: Double,
    zoom: Int,
): Double = (lon + 180.0) / 360.0 * (1 shl zoom)

internal fun latToTileYf(
    lat: Double,
    zoom: Int,
): Double {
    val latRad = lat * PI / 180.0
    return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)
}

internal fun tileXfToLon(
    tileXf: Double,
    zoom: Int,
): Double = tileXf / (1 shl zoom) * 360.0 - 180.0

internal fun tileYfToLat(
    tileYf: Double,
    zoom: Int,
): Double {
    val n = PI - 2.0 * PI * tileYf / (1 shl zoom)
    return 180.0 / PI * atan(sinh(n))
}
