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

package dev.adrc95.logronobus.core.ui.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor
import dev.adrc95.logronobus.core.domain.geo.bearingTo
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopEta
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import dev.adrc95.logronobus.core.ui.util.routeCodeComparator
import dev.adrc95.logronobus.core.ui.util.toLineColor
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

fun BusLocation.toDisplayModel(
    stops: List<StopDisplayModel> = emptyList(),
    resolvedNextStopRef: String? = null,
): BusLocationDisplayModel {
    val heading =
        bearing ?: (resolvedNextStopRef ?: nextStopRef)
            ?.let { ref -> stops.firstOrNull { it.stopRef == ref } }
            ?.let { next ->
                GeoPoint(latitude, longitude)
                    .bearingTo(GeoPoint(next.location.latitude, next.location.longitude))
            }
    return BusLocationDisplayModel(
        tripDuid = tripDuid,
        vehicleId = vehicleId,
        timetableId = timetableId,
        lineColor = lineColor.toLineColor(),
        labelColor = (lineColor.toLineColor() ?: Color(0xFFEA0046)).readableOnColor(),
        headsign = headsign,
        headingDegrees = heading,
        modifiedAtMillis =
            runCatching {
                modifiedTimestamp.toEpochMilliseconds()
            }.getOrNull(),
        location = LocationDisplayModel(latitude = latitude, longitude = longitude),
    )
}

fun List<BusLocation>.toDisplayModels(stops: List<StopDisplayModel> = emptyList()): List<BusLocationDisplayModel> =
    map { it.toDisplayModel(stops) }

fun GeoPoint.toDisplayModel(): LocationDisplayModel = LocationDisplayModel(latitude = lat, longitude = lon)

fun List<GeoPoint>.toLocationDisplayModels(): List<LocationDisplayModel> = map { it.toDisplayModel() }

fun List<GeoPoint>.toRouteLineDisplayModel(): RouteLineDisplayModel =
    RouteLineDisplayModel(points = toLocationDisplayModels().toImmutableList())

fun List<List<GeoPoint>>.toRouteDisplayModels(): List<RouteLineDisplayModel> =
    map { line -> line.toRouteLineDisplayModel() }

fun Stop.toDisplayModel(
    routeNames: Map<String, String> = emptyMap(),
    stopRefsByDirection: Map<String, List<Set<String>>> = emptyMap(),
): StopDisplayModel =
    StopDisplayModel(
        stopRef = stopRef,
        name = longName,
        location = LocationDisplayModel(latitude = latitude, longitude = longitude),
        stopId = stopId,
        routeNames =
            routes
                .orEmpty()
                .mapNotNull { code ->
                    routeNames[code]?.let { name -> name.inStopDirection(code, stopRef, stopRefsByDirection) }
                }.distinct()
                .toImmutableList(),
        lineCodes =
            routes
                .orEmpty()
                .distinct()
                .sortedWith(routeCodeComparator())
                .toImmutableList(),
    )

private fun String.inStopDirection(
    code: String,
    stopRef: String,
    stopRefsByDirection: Map<String, List<Set<String>>>,
): String {
    val directions = stopRefsByDirection[code].orEmpty()
    if (directions.size < 2) return this
    val inFirst = stopRef in directions[0]
    val inSecond = stopRef in directions[1]
    return if (!inFirst && inSecond) reversedPair() else this
}

private fun String.reversedPair(): String {
    val parts = split(" - ")
    return if (parts.size == 2) "${parts[1]} - ${parts[0]}" else this
}

fun List<Stop>.toStopDisplayModels(
    routeNames: Map<String, String> = emptyMap(),
    stopRefsByDirection: Map<String, List<Set<String>>> = emptyMap(),
): List<StopDisplayModel> = map { it.toDisplayModel(routeNames, stopRefsByDirection) }

fun StopEta.toDisplayModel(): StopEtaDisplayModel = StopEtaDisplayModel(label = label, upcoming = upcoming)

fun Map<String, StopEta>.toEtaDisplayModels(): Map<String, StopEtaDisplayModel> =
    mapValues { (_, eta) -> eta.toDisplayModel() }
