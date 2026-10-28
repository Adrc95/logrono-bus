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

package dev.adrc95.logronobus.core.domain.usecase

import dev.adrc95.logronobus.core.domain.fleet.isMeaningfullyStale
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.trip.forDirection
import dev.adrc95.logronobus.core.domain.trip.keepingTheFreshest
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.RouteBusPositions
import org.koin.core.annotation.Factory

@Factory
class GetRouteBusPositionsUseCase(
    private val vehicles: VehiclesRepository,
) {
    suspend operator fun invoke(
        routeNum: String,
        forceRefresh: Boolean = false,
    ): BusPositions = vehicles.getBusPositions(routeNum, forceRefresh = forceRefresh)

    suspend fun forRoute(
        routeNum: String,
        forceRefresh: Boolean = false,
    ): List<BusLocation> = vehicles.getBusPositions(routeNum, forceRefresh = forceRefresh).forRoute(routeNum)

    suspend fun forDirection(
        routeNum: String,
        direction: Int,
        stopRefsByDirection: List<Set<String>>,
        headsignsByDirection: List<String>,
        forceRefresh: Boolean = false,
    ): RouteBusPositions {
        val feed = vehicles.getBusPositions(routeNum, forceRefresh = forceRefresh)
        val all = feed.forRoute(routeNum)
        return RouteBusPositions(
            all = all,
            forDirection = all.forDirection(stopRefsByDirection, headsignsByDirection, direction),
            stale = feed.isMeaningfullyStale,
        )
    }

    fun keepingTheFreshest(
        previous: List<BusLocation>,
        current: List<BusLocation>,
    ): List<BusLocation> = current.keepingTheFreshest(previous)
}
