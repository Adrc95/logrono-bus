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

import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.Stop

class FakeCatalogueRepository(
    val stops: List<Stop> = emptyList(),
    val routes: Map<String, Route> = emptyMap(),
    val detail: RouteDetail? = null,
    val stopsByRoute: List<List<Stop>> = emptyList(),
    val shapes: List<List<GeoPoint>> = emptyList(),
    val failRouteDetail: Boolean = false,
) : CatalogueRepository {
    override suspend fun getRoutes(): Map<String, Route> = routes

    override suspend fun getStops(): List<Stop> = stops

    override suspend fun stopRefsByDirection(): Map<String, List<Set<String>>> {
        val lineStops = detail?.stops ?: stopsByRoute
        if (lineStops.isEmpty()) return emptyMap()
        return routes.keys.associateWith {
            lineStops.map { direction -> direction.mapTo(mutableSetOf()) { stop -> stop.stopRef } }
        }
    }

    override suspend fun getStop(stopRef: String): Stop? =
        stops.firstOrNull { it.stopRef == stopRef }
            ?: stopsByRoute.flatten().firstOrNull { it.stopRef == stopRef }
            ?: detail?.stops?.flatten()?.firstOrNull { it.stopRef == stopRef }

    override suspend fun getStopsForRoute(routeNum: String): List<List<Stop>> {
        if (failRouteDetail) error("the route detail is down")
        return detail?.stops ?: stopsByRoute
    }

    override suspend fun getDirectionLabels(routeNum: String): List<String> = detail?.labels.orEmpty()

    override suspend fun getDirectionOrigins(routeNum: String): List<String> = detail?.origins.orEmpty()

    override suspend fun getDirectionHeadsigns(routeNum: String): List<String> = detail?.headsigns.orEmpty()

    override suspend fun getRouteShapes(routeNum: String): List<List<GeoPoint>> = detail?.shapes ?: shapes

    override suspend fun getShape(shapeId: String): List<GeoPoint> = emptyList()

    override suspend fun getStopLineLabels(stopId: String): Map<String, String> = emptyMap()
}
