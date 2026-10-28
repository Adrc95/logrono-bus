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

package dev.adrc95.logronobus.core.testing.fake

import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.mapper.visibleCode

class FakeCatalogueDataSource(
    val codes: Map<Int, String> = emptyMap(),
    val stopsByLine: Map<String, List<List<Stop>>> = emptyMap(),
    val colours: Map<String, String> = emptyMap(),
    val stops: List<Stop> = emptyList(),
    val routes: Map<String, Route> = emptyMap(),
    val stopSequences: Map<String, List<StopSequence>> = emptyMap(),
) : CatalogueDataSource {
    override suspend fun visibleCodes(): Map<Int, String> = codes

    override suspend fun fleetCodeResolver(): (String, String?) -> String? =
        { ref, _ ->
            (ref.toIntOrNull()?.let { codes[it] } ?: visibleCode(ref)).takeIf { it.isNotEmpty() }
        }

    override suspend fun internalIdOf(code: String): Int? = codes.entries.firstOrNull { it.value == code }?.key

    override suspend fun lineColours(): Map<String, String> = colours

    override suspend fun getRoutes(): Map<String, Route> = routes

    override suspend fun getStops(): List<Stop> = stops

    override suspend fun sequences(): Map<String, List<StopSequence>> = stopSequences

    override suspend fun getStopsForRoute(routeNum: String): List<List<Stop>> = stopsByLine[routeNum].orEmpty()

    override suspend fun stopRefsByDirection(): Map<String, List<Set<String>>> =
        stopsByLine.mapValues { (_, directions) ->
            directions.map { stops -> stops.mapTo(mutableSetOf()) { it.stopRef } }
        }

    override suspend fun getDirectionLabels(routeNum: String): List<String> = emptyList()

    override suspend fun getDirectionOrigins(routeNum: String): List<String> = emptyList()

    override suspend fun getDirectionHeadsigns(routeNum: String): List<String> = emptyList()

    override suspend fun getStopLineLabels(stopId: String): Map<String, String> = emptyMap()

    override suspend fun directionNameFor(
        code: String,
        directionIndex: Int,
    ): String? = null

    override suspend fun internalLinesAt(id: Int): List<Int> = emptyList()

    override suspend fun codeForArrival(
        lineRef: String,
        stopId: Int,
        order: Int?,
        sequences: Map<String, List<StopSequence>>,
    ): String = visibleCode(lineRef)

    override suspend fun getRouteShapes(routeNum: String): List<List<GeoPoint>> = emptyList()

    override suspend fun getShape(shapeId: String): List<GeoPoint> = emptyList()
}
