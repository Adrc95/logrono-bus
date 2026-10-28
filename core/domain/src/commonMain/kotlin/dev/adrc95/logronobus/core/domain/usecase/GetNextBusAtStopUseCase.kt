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

import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.trip.keepingTheLaterHour
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.DepartureTime
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class GetNextBusAtStopUseCase(
    private val departures: DeparturesRepository,
) {
    suspend operator fun invoke(
        lineCode: String,
        stopRef: String,
        followedAt: Instant?,
        pinnedTripRef: String?,
        followedVehicleId: String?,
        followedTripId: String?,
    ): Arrival? {
        val arrivals = departures.getLineArrivals(lineCode)
        val atStop = arrivals.filter { it.stopRef == stopRef && !it.cancellation }
        val next =
            nextArrivalAt(
                arrivals = arrivals,
                stopRef = stopRef,
                followedAt = followedAt,
                pinnedTripRef = pinnedTripRef,
                followedVehicleId = followedVehicleId,
                followedTripId = followedTripId,
            )
        logDebug(
            "nextBus line=$lineCode stop=$stopRef followed=$followedAt pinned=$pinnedTripRef " +
                "excluded=[$pinnedTripRef,$followedVehicleId,$followedTripId] " +
                "candidates=${atStop.joinToString { "${it.vehicleRef}@${it.expected}" }} " +
                "-> ${next?.vehicleRef}@${next?.expected}",
        )
        return next
    }

    fun laterHour(
        current: DepartureTime?,
        candidate: DepartureTime?,
    ): DepartureTime? = current.keepingTheLaterHour(candidate)
}

fun nextArrivalAt(
    arrivals: List<Arrival>,
    stopRef: String,
    followedAt: Instant?,
    pinnedTripRef: String? = null,
    followedVehicleId: String? = null,
    followedTripId: String? = null,
): Arrival? =
    arrivals
        .filter { it.stopRef == stopRef && !it.cancellation }
        .filterNot { arrival ->
            val ref = arrival.vehicleRef?.takeIf { it.isNotBlank() } ?: return@filterNot false
            ref == pinnedTripRef || ref == followedVehicleId || ref == followedTripId
        }.mapNotNull { arrival -> arrival.expected?.let { at -> at to arrival } }
        .filter { (at, _) -> followedAt == null || at > followedAt }
        .minByOrNull { (at, _) -> at }
        ?.second
