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

import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.data.vehicles.NightArrivalEstimator
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.FleetDataSource
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory(binds = [VehiclesRepository::class])
class VehiclesRepositoryImpl(
    private val fleet: FleetDataSource,
    private val catalogue: CatalogueDataSource,
    private val theoretical: NightArrivalEstimator,
) : VehiclesRepository {
    override suspend fun getBusPositions(
        now: Instant,
        forceRefresh: Boolean,
    ): BusPositions =
        fleet
            .positions(now, forceRefresh)
            .getOrStale()
            .withTheoreticalArrivals(now)

    override suspend fun getBusPositions(
        routeNum: String,
        now: Instant,
        forceRefresh: Boolean,
    ): BusPositions =
        fleet
            .positionsFor(routeNum, now, forceRefresh)
            .getOrStale()
            .withTheoreticalArrivals(now)

    private suspend fun BusPositions.withTheoreticalArrivals(now: Instant): BusPositions =
        theoretical.withTheoreticalArrivals(this, now, catalogue::internalIdOf)

    private fun Result<BusPositions>.getOrStale(): BusPositions =
        when (this) {
            is Result.Success -> data
            is Result.Error -> BusPositions(emptyMap(), stale = true)
        }
}
