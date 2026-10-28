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

import dev.adrc95.logronobus.core.domain.geo.nearestTo
import dev.adrc95.logronobus.core.domain.geo.withWalkingDistances
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.model.WalkDestination
import org.koin.core.annotation.Factory

@Factory
class GetNearbyStopsUseCase(
    private val getStops: GetStopsUseCase,
    private val getWalkDistances: GetWalkDistancesUseCase,
) {
    suspend fun nearbyStops(
        location: GeoPoint,
        limit: Int,
    ): List<NearbyStop> = getStops().nearestTo(location, limit)

    suspend fun withWalkingTimes(
        location: GeoPoint,
        nearby: List<NearbyStop>,
        walkLimit: Int,
    ): List<NearbyStop> {
        val walked =
            getWalkDistances(
                origin = location,
                destinations =
                    nearby.take(walkLimit).map { candidate ->
                        WalkDestination(
                            id = candidate.stop.stopRef,
                            lat = candidate.stop.latitude,
                            lng = candidate.stop.longitude,
                        )
                    },
            )
        return nearby.withWalkingDistances(walked)
    }
}
