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

import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.trip.pinnedTrip
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.PinnedArrivals
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class GetPinnedArrivalsUseCase(
    private val getLineArrivals: GetLineArrivalsUseCase,
) {
    suspend operator fun invoke(
        code: String,
        followedStopRef: String,
        followedAt: Instant?,
        followedVehicleRef: String?,
        pinned: String?,
    ): PinnedArrivals {
        val arrivals = getLineArrivals(code)
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = followedStopRef,
                followedAt = followedAt,
                followedVehicleRef = followedVehicleRef,
                pinned = pinned,
            )
        val rows =
            if (trip == null) {
                emptyList()
            } else {
                arrivals
                    .filter { it.vehicleRef == trip }
                    .mapNotNull { arrival ->
                        val stop = arrival.stopRef ?: return@mapNotNull null
                        val at = arrival.expected ?: arrival.aimed ?: return@mapNotNull null
                        stop to at
                    }
            }
        val byStop = rows.groupBy({ it.first }, { it.second })
        val now = Instant.fromEpochMilliseconds(nowEpochMilliseconds())
        val predictions =
            byStop.mapValues { (_, times) ->
                times.filter { it > now - DomainConstants.STALE_PREDICTION_GRACE }.minOrNull()
                    ?: times.max()
            }
        return PinnedArrivals(arrivals = arrivals, trip = trip, predictions = predictions)
    }
}
