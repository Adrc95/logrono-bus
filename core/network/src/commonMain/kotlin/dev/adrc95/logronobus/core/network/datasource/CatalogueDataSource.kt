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

package dev.adrc95.logronobus.core.network.datasource

import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopSequence

interface CatalogueDataSource {
    suspend fun getRoutes(): Map<String, Route>

    suspend fun getStops(): List<Stop>

    suspend fun stopRefsByDirection(): Map<String, List<Set<String>>>

    suspend fun sequences(): Map<String, List<StopSequence>>

    suspend fun internalLinesAt(id: Int): List<Int>

    suspend fun codeForArrival(
        lineRef: String,
        stopId: Int,
        order: Int?,
        sequences: Map<String, List<StopSequence>>,
    ): String

    suspend fun visibleCodes(): Map<Int, String>

    suspend fun internalIdOf(code: String): Int?

    suspend fun fleetCodeResolver(): (
        lineRef: String,
        publishedName: String?,
    ) -> String?

    suspend fun lineColours(): Map<String, String>

    suspend fun getStopsForRoute(routeNum: String): List<List<Stop>>

    suspend fun getDirectionLabels(routeNum: String): List<String>

    suspend fun getDirectionOrigins(routeNum: String): List<String>

    suspend fun getDirectionHeadsigns(routeNum: String): List<String>

    suspend fun getRouteShapes(routeNum: String): List<List<GeoPoint>>

    suspend fun getShape(shapeId: String): List<GeoPoint>

    suspend fun getStopLineLabels(stopId: String): Map<String, String>

    suspend fun directionNameFor(
        code: String,
        directionIndex: Int,
    ): String?
}
