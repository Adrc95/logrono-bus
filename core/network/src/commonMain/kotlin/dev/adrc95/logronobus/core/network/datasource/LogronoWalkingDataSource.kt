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

import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.common.exception.map
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.WalkDestination
import dev.adrc95.logronobus.core.model.WalkDistance
import dev.adrc95.logronobus.core.network.dto.LatLngDto
import dev.adrc95.logronobus.core.network.dto.WalkDestinationDto
import dev.adrc95.logronobus.core.network.dto.WalkRequestDto
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.service.WalkingService
import dev.adrc95.logronobus.core.network.tryCall
import kotlinx.serialization.json.JsonPrimitive
import org.koin.core.annotation.Factory

@Factory(binds = [WalkingDataSource::class])
class LogronoWalkingDataSource(
    private val service: WalkingService,
) : WalkingDataSource {
    override suspend fun walkDistances(
        origin: GeoPoint,
        destinations: List<WalkDestination>,
    ): Result<List<WalkDistance>> {
        if (destinations.isEmpty()) return Result.Success(emptyList())
        val request =
            WalkRequestDto(
                origin = LatLngDto(lat = origin.lat, lng = origin.lon),
                destinations = destinations.map { it.asApiDestination() },
            )
        return tryCall { service.getWalkDistances(request) }.map { it.toDomain(destinations) }
    }

    private fun WalkDestination.asApiDestination(): WalkDestinationDto {
        val numeric = id.toIntOrNull()
        return WalkDestinationDto(
            id = if (numeric != null) JsonPrimitive(numeric) else JsonPrimitive(id),
            lat = lat,
            lng = lng,
        )
    }
}
