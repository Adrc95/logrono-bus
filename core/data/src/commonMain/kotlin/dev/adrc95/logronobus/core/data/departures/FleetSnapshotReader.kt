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

package dev.adrc95.logronobus.core.data.departures

import dev.adrc95.logronobus.core.common.exception.getOrNull
import dev.adrc95.logronobus.core.domain.trip.behindAt
import dev.adrc95.logronobus.core.domain.trip.callsAt
import dev.adrc95.logronobus.core.domain.trip.departureInstantOf
import dev.adrc95.logronobus.core.domain.trip.fleetClock
import dev.adrc95.logronobus.core.domain.trip.inOrder
import dev.adrc95.logronobus.core.domain.trip.usableActivities
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FleetView
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.FleetDataSource
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import dev.adrc95.logronobus.core.domain.trip.withFleet as mergeFleet

@Factory
class FleetSnapshotReader(
    private val fleet: FleetDataSource,
    private val catalogue: CatalogueDataSource,
) {
    suspend fun snapshotAt(
        stopId: Int,
        internalLines: List<Int>,
        direction: Int?,
        now: Instant,
        windowMinutes: Int,
        sequences: Map<String, List<StopSequence>>,
    ): FleetView {
        val feed =
            fleet.feedForLines(internalLines, now).getOrNull()
                ?: return FleetView(emptyList(), emptyList(), stopId, sequences)
        val codes = catalogue.visibleCodes()
        val clock = fleetClock(feed.serverTimeMs, now)
        val activities =
            usableActivities(
                activities = feed.activities,
                servingLines = internalLines.toSet(),
                visibleLines = codes.keys,
                clock = clock,
                freshMinutes = NetworkConstants.DEFAULT_FRESH_MINUTES,
            )
        return FleetView(
            calls = callsAt(stopId, activities, clock, codes, direction, windowMinutes, sequences),
            behind = behindAt(activities, codes, direction, sequences),
            stopId = stopId,
            sequences = sequences,
        )
    }

    fun withFleet(
        rows: List<DepartureTime>,
        view: FleetView,
    ): List<DepartureTime> = inOrder(mergeFleet(rows, view, ::departureInstantOf))
}
