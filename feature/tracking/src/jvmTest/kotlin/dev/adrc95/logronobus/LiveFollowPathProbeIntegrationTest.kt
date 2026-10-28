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

package dev.adrc95.logronobus

import androidx.lifecycle.viewmodel.compose.viewModel
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.data.repository.TrackedTripRepositoryImpl
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetLineArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNextBusAtStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPinnedArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteShapeUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopEtasUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackedTripUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.UpdateTrackedTripUseCase
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import dev.adrc95.logronobus.feature.tracking.TrackingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assume
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class LiveFollowPathProbeIntegrationTest {
    @BeforeTest
    fun require_live_api() {
        Assume.assumeTrue(System.getenv("LIVE_API") == "1")
    }

    @Test
    fun `given a followed row from the favourites screen when the screen is built then it has a bus it can draw`() =
        runTest {
            val repository = TestBus()
            val trackedTrip = TrackedTripRepositoryImpl()
            val tracking =
                TrackingViewModel(
                    GetRouteShapeUseCase(repository.catalogue),
                    GetRouteBusPositionsUseCase(repository.vehicles),
                    GetRouteDetailUseCase(repository.catalogue),
                    GetRoutesUseCase(repository.catalogue),
                    GetNextBusAtStopUseCase(repository.departures),
                    GetTrackedTripUseCase(trackedTrip),
                    GetPinnedArrivalsUseCase(GetLineArrivalsUseCase(repository.departures)),
                    UpdateTrackedTripUseCase(trackedTrip),
                )
            val routes =
                RoutesViewModel(
                    GetRouteDetailUseCase(repository.catalogue),
                    GetRouteShapeUseCase(repository.catalogue),
                    GetRouteBusPositionsUseCase(repository.vehicles),
                    GetStopEtasUseCase(),
                    GetStopDeparturesUseCase(repository.departures),
                    GetLastViewedRouteUseCase(repository.lastViewedRoute),
                    SaveLastViewedRouteUseCase(repository.lastViewedRoute),
                    GetRoutesUseCase(repository.catalogue),
                    GetFavouriteStopsUseCase(repository.favourites),
                    ToggleFavouriteUseCase(repository.favourites),
                    GetTrackedTripUseCase(trackedTrip),
                    UpdateTrackedTripUseCase(trackedTrip),
                    StartTrackingUseCase(trackedTrip, SaveLastViewedRouteUseCase(repository.lastViewedRoute)),
                )
            val now = Instant.fromEpochMilliseconds(nowEpochMilliseconds())
            for (stopRef in listOf("88", "157")) {
                val rows = repository.departures.getStopDeparturesWithLive(stopRef, now = now).times
                rows.forEach { row ->
                    println(
                        "live: row @$stopRef line=${row.timetableId} dir=${row.directionIndex} " +
                            "vehicle=${row.vehicleId} trip=${row.tripId} status=${row.arrivalStatus} " +
                            "at=${row.departTimestamp}",
                    )
                }
                val row = rows.firstOrNull { it.vehicleId != null }
                if (row == null) {
                    println("live: @$stopRef no row carries a bus at all")
                    continue
                }
                tracking.setTrackedDeparture(row, stopRef)
                // The screen only holds positions once a line is open: tracking alone does not fetch
                // them, and without this the probe always read an empty list.
                routes.selectRoute(row.timetableId)
                delay(4_000)
                val positions = routes.uiState.value.busPositions
                val drawn =
                    positions.filter {
                        (row.vehicleId != null && it.vehicleId == row.vehicleId) || it.tripDuid == row.tripId
                    }
                println(
                    "live: followed line=${row.timetableId} tapped=${row.vehicleId}/${row.tripId} " +
                        "held=${tracking.uiState.value.tracking.departure?.timetableId}/${tracking.uiState.value.tracking.trackedTripId}",
                )
                println(
                    "live: positionsOnLine=${positions.mapNotNull { it.vehicleId }} " +
                        "drawn=${drawn.mapNotNull { it.vehicleId }}",
                )
                val arrivals = tracking.uiState.value.tracking.stopEtas
                println("live: lineArrivals=${arrivals.size} $arrivals")
                println("live: now=${nowEpochMilliseconds()}")
                tracking.clearTrackedDeparture()
            }
        }
}
