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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.domain.DomainConstants.MAX_BUS_METRES_PER_SECOND
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.GeoPoint

fun extrapolationCapMs(gapBetweenReportsMs: Long): Long = gapBetweenReportsMs.coerceIn(10_000L, 45_000L)

fun metresToDraw(
    observedMetresPerSecond: Double,
    elapsedMs: Long,
    remainingMetres: Double,
): Double {
    if (elapsedMs <= 0L || remainingMetres <= 0.0) return 0.0
    val pace = observedMetresPerSecond.coerceIn(0.0, MAX_BUS_METRES_PER_SECOND)
    return minOf(pace * (elapsedMs / 1000.0), remainingMetres)
}

fun advanceAlongRoute(
    shape: List<GeoPoint>,
    from: GeoPoint,
    to: GeoPoint,
    progress: Double,
): GeoPoint {
    val fraction = progress.coerceIn(0.0, 1.0)
    if (shape.size < 2) return alongChord(from, to, fraction)
    val start = shape.nearestIndexTo(from)
    val end = shape.nearestIndexTo(to)
    if (end <= start) return alongChord(from, to, fraction)
    val stretch = shape.subList(start, end + 1)
    var total = 0.0
    for (index in 0 until stretch.size - 1) total += stretch[index].distanceTo(stretch[index + 1])
    var wanted = fraction * total
    for (index in 0 until stretch.size - 1) {
        val leg = stretch[index].distanceTo(stretch[index + 1])
        if (wanted <= leg) {
            return alongChord(
                stretch[index],
                stretch[index + 1],
                if (leg <= 0.0) 0.0 else wanted / leg,
            )
        }
        wanted -= leg
    }
    return stretch.last()
}

fun routeMetresTo(
    shape: List<GeoPoint>,
    point: GeoPoint,
): Double {
    if (shape.size < 2) return 0.0
    val end = shape.nearestIndexTo(point)
    var metres = 0.0
    for (index in 0 until end) metres += shape[index].distanceTo(shape[index + 1])
    return metres
}

fun observedPace(
    metresAlongRoute: Double,
    seconds: Double,
): Double = if (seconds <= 0.0 || metresAlongRoute <= 0.0) 0.0 else metresAlongRoute / seconds

fun drawingMayMoveTo(
    candidateMetres: Double,
    drawnBeforeMetres: Double?,
): Boolean = drawnBeforeMetres == null || candidateMetres >= drawnBeforeMetres

private fun List<GeoPoint>.nearestIndexTo(point: GeoPoint): Int {
    var best = 0
    var bestMetres = Double.MAX_VALUE
    forEachIndexed { index, vertex ->
        val metres = vertex.distanceTo(point)
        if (metres < bestMetres) {
            bestMetres = metres
            best = index
        }
    }
    return best
}

private fun alongChord(
    a: GeoPoint,
    b: GeoPoint,
    fraction: Double,
): GeoPoint = GeoPoint(lat = a.lat + (b.lat - a.lat) * fraction, lon = a.lon + (b.lon - a.lon) * fraction)
