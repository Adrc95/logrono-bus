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

package dev.adrc95.logronobus.core.network.mapper

import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.dto.LatLngDto
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto

fun LineDto.toDomain(): Route {
    val code = visibleCode(name)
    return Route(
        timetableId = code,
        longName = name,
        shortName = code,
        color = color,
        internalId = id,
    )
}

fun LineDto.directionStops(): List<List<Stop>> =
    directions().map { direction -> direction.oneStopPerKerbside().map { it.toDomain() } }

fun LineDto.directionHeadsigns(): List<String> =
    directions().mapNotNull { it.lastOrNull()?.name?.takeIf(String::isNotBlank) }

fun LineDto.directionShapes(): List<List<GeoPoint>> =
    directions().map { direction ->
        direction.oneStopPerKerbside().flatMap { stop ->
            stop.onwardShape.map { it.toDomain() }
        }
    }

private fun LineDto.directions(): List<List<LineStopDto>> = listOf(stops.asc, stops.desc).filter { it.isNotEmpty() }

private fun List<LineStopDto>.oneStopPerKerbside(): List<LineStopDto> =
    fold(mutableListOf()) { kept, stop ->
        val previous = kept.lastOrNull()
        val samePlace =
            previous != null &&
                previous.id != stop.id &&
                previous.name.trim().equals(stop.name.trim(), ignoreCase = true) &&
                previous.toGeoPoint().distanceTo(stop.toGeoPoint()) <=
                NetworkConstants.TWIN_KERB_METRES
        if (!samePlace) kept.add(stop)
        kept
    }

private fun LineStopDto.toDomain(): Stop =
    Stop(
        stopRef = id.toString(),
        stopId = id.toString(),
        longName = name,
        shortName = name,
        latitude = lat,
        longitude = lng,
        routes = null,
        direction = null,
    )

private fun LineStopDto.toGeoPoint(): GeoPoint = GeoPoint(lat = lat, lon = lng)

private fun LatLngDto.toDomain(): GeoPoint = GeoPoint(lat = lat, lon = lng)

fun visibleCode(lineName: String): String {
    val code = lineName.substringBefore('-').trim()
    return if (code.isBlank() || code == NetworkConstants.NO_LINE) "" else code
}
