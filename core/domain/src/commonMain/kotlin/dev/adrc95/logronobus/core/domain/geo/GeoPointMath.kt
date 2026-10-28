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

package dev.adrc95.logronobus.core.domain.geo

import dev.adrc95.logronobus.core.model.GeoPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun GeoPoint.distanceTo(other: GeoPoint): Double {
    val earthRadius = 6_371_000.0
    val dLat = (other.lat - lat).toRadians()
    val dLon = (other.lon - lon).toRadians()
    val a =
        sin(dLat / 2).pow(2) +
            cos(lat.toRadians()) * cos(other.lat.toRadians()) * sin(dLon / 2).pow(2)
    return 2 * earthRadius * asin(min(1.0, sqrt(a)))
}

fun GeoPoint.bearingTo(other: GeoPoint): Float? {
    val dLat = other.lat - lat
    val dLon = (other.lon - lon) * cos(lat * PI / 180.0)
    if (abs(dLat) + abs(dLon) < 1e-9) return null
    val degrees = atan2(dLon, dLat) * 180.0 / PI
    return ((degrees % 360.0 + 360.0) % 360.0).toFloat()
}

private fun Double.toRadians() = this / 180.0 * PI
