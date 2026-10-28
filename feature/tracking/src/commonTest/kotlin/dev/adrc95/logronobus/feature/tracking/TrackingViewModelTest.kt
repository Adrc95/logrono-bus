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

package dev.adrc95.logronobus.feature.tracking

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.usecase.GetLineArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNextBusAtStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPinnedArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteShapeUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackedTripUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackingViewUseCase
import dev.adrc95.logronobus.core.domain.usecase.UpdateTrackedTripUseCase
import dev.adrc95.logronobus.core.model.RiderStopState
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.busLocation
import dev.adrc95.logronobus.core.testing.builder.busPositions
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.route
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import dev.adrc95.logronobus.core.testing.fake.FakeDeparturesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeTrackedTripRepository
import dev.adrc95.logronobus.core.testing.fake.FakeVehiclesRepository
import dev.adrc95.logronobus.feature.tracking.model.DelayStatus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ROUTE = "10"

private val firstDirection =
    listOf(
        stop {
            withStopRef("S1")
            withStopId("S1")
            withLongName("Murrieta")
            withShortName("Murrieta")
            withLatitude(42.46)
            withLongitude(-2.4400)
        },
        stop {
            withStopRef("S2")
            withStopId("S2")
            withLongName("Portales")
            withShortName("Portales")
            withLatitude(42.46)
            withLongitude(-2.4370)
        },
        stop {
            withStopRef("S3")
            withStopId("S3")
            withLongName("Ayuntamiento")
            withShortName("Ayuntamiento")
            withLatitude(42.46)
            withLongitude(-2.4340)
        },
    )

private val secondDirection =
    listOf(
        stop {
            withStopRef("T1")
            withStopId("T1")
            withLongName("El Cortijo")
            withShortName("El Cortijo")
            withLatitude(42.47)
            withLongitude(-2.4300)
        },
        stop {
            withStopRef("T2")
            withStopId("T2")
            withLongName("Valdegastea")
            withShortName("Valdegastea")
            withLatitude(42.48)
            withLongitude(-2.4200)
        },
    )

private val routes =
    mapOf(
        ROUTE to
            route {
                withTimetableId(ROUTE)
                withShortName(ROUTE)
                withLongName("10-HOSPITAL SAN PEDRO-CENTRO")
            },
    )

private val routeDetail =
    RouteDetail(
        stops = listOf(firstDirection, secondDirection),
        labels = listOf("HOSPITAL SAN PEDRO", "CENTRO"),
        origins = listOf("HOSPITAL SAN PEDRO", "CENTRO"),
        headsigns = listOf("Hospital San Pedro", "Centro"),
        shapes = emptyList(),
    )

private val trackedDeparture =
    departure {
        withDisplayName("Hospital San Pedro")
        withTimetableId(ROUTE)
        withTripId("trip-1")
        withVehicleId("V1")
        withHeadsign("Hospital San Pedro")
        withDirectionIndex(0)
        withDelaySeconds(0)
    }

private val trackedRide =
    TrackedTrip(
        tripId = "trip-1",
        stopRef = "S2",
        departure = trackedDeparture,
    )

private val linePositions =
    busPositions {
        withByRoute(
            mapOf(
                ROUTE to
                    listOf(
                        busLocation {
                            withLatitude(42.46)
                            withLongitude(-2.4400)
                            withTripDuid("trip-1")
                            withVehicleId("V1")
                            withTimetableId(ROUTE)
                            withHeadsign("Hospital San Pedro")
                            withNextStopRef("S2")
                        },
                    ),
            ),
        )
    }

class TrackingViewModelTest {
    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(
        catalogue: CatalogueRepository = FakeCatalogueRepository(routes = routes, detail = routeDetail),
        vehicles: VehiclesRepository = FakeVehiclesRepository(),
        tracked: TrackedTrip? = null,
    ): TrackingViewModel {
        val departures = FakeDeparturesRepository()
        val trackedTrips = FakeTrackedTripRepository(tracked)
        return TrackingViewModel(
            getTrackingView = GetTrackingViewUseCase(),
            getRouteShape = GetRouteShapeUseCase(catalogue),
            getRouteBusPositions = GetRouteBusPositionsUseCase(vehicles),
            getRouteDetail = GetRouteDetailUseCase(catalogue),
            getRoutes = GetRoutesUseCase(catalogue),
            getNextBusAtStop = GetNextBusAtStopUseCase(departures),
            getTrackedTrip = GetTrackedTripUseCase(trackedTrips),
            getPinnedArrivals = GetPinnedArrivalsUseCase(GetLineArrivalsUseCase(departures)),
            updateTrackedTrip = UpdateTrackedTripUseCase(trackedTrips),
        ).also { mainDispatcherRule.track(it.viewModelScope) }
    }

    @Test
    fun `given nothing is tracked when the tracking screen opens then no departure is followed`() =
        mainDispatcherRule.runViewModelTest {
            // When
            val viewModel = createViewModel()

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.tracking.mapPolyline.isEmpty()) {
                    state = awaitItem()
                }

                assertNull(state.tracking.departure)
                assertTrue(state.tracking.timelineStops.isEmpty())
                assertNull(state.tracking.trackedBus)
                assertNull(state.tracking.trackedTripId)
                assertNull(state.tracking.trackedStopRef)
                assertNull(state.tracking.riderStopName)
                assertEquals(RiderStopState.Unknown, state.tracking.riderStopState)
                assertEquals(DelayStatus.Unknown, state.tracking.delay)
                assertFalse(state.tracking.nextBusKnown)
                assertFalse(state.tracking.completed)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a tracked trip when its route is read then the timeline and the followed bus are shown`() =
        mainDispatcherRule.runViewModelTest {
            // When
            val viewModel =
                createViewModel(
                    vehicles = FakeVehiclesRepository(linePositions),
                    tracked = trackedRide,
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.tracking.timelineStops.isEmpty() || state.tracking.trackedBus == null) {
                    state = awaitItem()
                }

                assertEquals("Hospital San Pedro", state.tracking.departure?.displayName)
                assertEquals(ROUTE, state.tracking.departure?.timetableId)
                assertEquals(listOf("S1", "S2", "S3"), state.tracking.timelineStops.map { it.stopRef })
                assertEquals("Portales", state.tracking.riderStopName)
                assertEquals("S2", state.tracking.trackedStopRef)
                assertEquals("trip-1", state.tracking.trackedTripId)
                assertEquals("V1", state.tracking.trackedBus?.vehicleId)
                assertEquals(listOf("V1"), state.tracking.positions.map { it.vehicleId })
                assertEquals(RiderStopState.Approaching, state.tracking.riderStopState)
                assertEquals(DelayStatus.OnTime, state.tracking.delay)
                assertFalse(state.tracking.completed)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the route detail cannot be read when the tracked trip is applied then the departure is shown without a timeline`() =
        mainDispatcherRule.runViewModelTest {
            // When
            val viewModel =
                createViewModel(
                    catalogue = FakeCatalogueRepository(failRouteDetail = true),
                    tracked = trackedRide,
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.tracking.departure == null) {
                    state = awaitItem()
                }

                assertEquals("Hospital San Pedro", state.tracking.departure?.displayName)
                assertEquals("S2", state.tracking.trackedStopRef)
                assertEquals("trip-1", state.tracking.trackedTripId)
                assertTrue(state.tracking.timelineStops.isEmpty())
                assertNull(state.tracking.riderStopName)
                assertNull(state.tracking.trackedBus)
                assertTrue(state.tracking.positions.isEmpty())
                assertEquals(RiderStopState.Unknown, state.tracking.riderStopState)
                assertFalse(state.tracking.completed)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
