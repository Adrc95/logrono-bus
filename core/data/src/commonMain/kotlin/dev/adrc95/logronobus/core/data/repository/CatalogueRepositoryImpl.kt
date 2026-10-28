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

package dev.adrc95.logronobus.core.data.repository

import dev.adrc95.logronobus.core.data.CacheStrategy
import dev.adrc95.logronobus.core.database.datasource.CatalogueLocalDataSource
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [CatalogueRepository::class])
class CatalogueRepositoryImpl(
    private val remote: CatalogueDataSource,
    private val local: CatalogueLocalDataSource? = null,
    @Named("OfflineFirst")
    private val cache: CacheStrategy? = null,
) : CatalogueRepository {
    override suspend fun getRoutes(): Map<String, Route> =
        if (cache != null && local != null) {
            val routes =
                cache.execute(
                    remote = { remote.getRoutes().values.toList() },
                    local = { local.getLines() },
                    save = { local.saveLines(it) },
                    isExpired = { local.isExpired() },
                )
            routes.associateBy { it.timetableId }
        } else {
            remote.getRoutes()
        }

    override suspend fun getStops(): List<Stop> =
        if (cache != null && local != null) {
            getRoutes()
            cache.execute(
                remote = { remote.getStops() },
                local = { local.getStops() },
                save = { local.saveStops(it) },
                isExpired = { local.isExpired() },
            )
        } else {
            remote.getStops()
        }

    override suspend fun getStopsForRoute(routeNum: String): List<List<Stop>> = remote.getStopsForRoute(routeNum)

    override suspend fun stopRefsByDirection(): Map<String, List<Set<String>>> = remote.stopRefsByDirection()

    override suspend fun getStop(stopRef: String): Stop? =
        getStops().firstOrNull { it.stopRef == stopRef }
            ?: getRoutes().keys.firstNotNullOfOrNull { route ->
                getStopsForRoute(route).flatten().firstOrNull { it.stopRef == stopRef }
            }

    override suspend fun getDirectionLabels(routeNum: String): List<String> = remote.getDirectionLabels(routeNum)

    override suspend fun getDirectionOrigins(routeNum: String): List<String> = remote.getDirectionOrigins(routeNum)

    override suspend fun getDirectionHeadsigns(routeNum: String): List<String> = remote.getDirectionHeadsigns(routeNum)

    override suspend fun getRouteShapes(routeNum: String): List<List<GeoPoint>> = remote.getRouteShapes(routeNum)

    override suspend fun getShape(shapeId: String): List<GeoPoint> = remote.getShape(shapeId)

    override suspend fun getStopLineLabels(stopId: String): Map<String, String> = remote.getStopLineLabels(stopId)
}
