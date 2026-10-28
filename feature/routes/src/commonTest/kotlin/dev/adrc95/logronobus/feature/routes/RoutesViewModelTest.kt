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

package dev.adrc95.logronobus.feature.routes

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.domain.repository.LastViewedRouteRepository
import dev.adrc95.logronobus.core.domain.repository.TrackedTripRepository
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetLastViewedRouteUseCase
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
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.busLocation
import dev.adrc95.logronobus.core.testing.builder.busPositions
import dev.adrc95.logronobus.core.testing.builder.route
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import dev.adrc95.logronobus.core.testing.fake.FakeDeparturesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeFavouritesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeLastViewedRouteRepository
import dev.adrc95.logronobus.core.testing.fake.FakeTrackedTripRepository
import dev.adrc95.logronobus.core.testing.fake.FakeVehiclesRepository
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

private const val ROUTE = "6"

private val firstDirection =
    listOf(
        stop {
            withStopRef("R1-1")
            withStopId("R1-1")
            withLongName("Murrieta")
            withShortName("Murrieta")
        },
        stop {
            withStopRef("R1-2")
            withStopId("R1-2")
            withLongName("Portales")
            withShortName("Portales")
        },
        stop {
            withStopRef("R1-3")
            withStopId("R1-3")
            withLongName("Ayuntamiento")
            withShortName("Ayuntamiento")
        },
    )

private val secondDirection =
    listOf(
        stop {
            withStopRef("R2-1")
            withStopId("R2-1")
            withLongName("El Cortijo")
            withShortName("El Cortijo")
        },
        stop {
            withStopRef("R2-2")
            withStopId("R2-2")
            withLongName("Valdegastea")
            withShortName("Valdegastea")
        },
    )

private val routes =
    mapOf(
        ROUTE to
            route {
                withTimetableId(ROUTE)
                withShortName(ROUTE)
                withLongName("6-CENTRO-CORTIJO")
            },
        "4" to
            route {
                withTimetableId("4")
                withShortName("4")
                withLongName("4-PORTALES-VALDEGASTEA")
            },
    )

private val routeDetail =
    RouteDetail(
        stops = listOf(firstDirection, secondDirection),
        labels = listOf("CENTRO", "EL CORTIJO"),
        origins = listOf("CENTRO", "CORTIJO"),
        headsigns = listOf("CENTRO", "CORTIJO"),
        shapes = emptyList(),
    )

private val routePositions =
    busPositions {
        withByRoute(
            mapOf(
                ROUTE to
                    listOf(
                        busLocation {
                            withTripDuid("trip-1")
                            withVehicleId("V1")
                            withTimetableId(ROUTE)
                            withNextStopRef("R1-2")
                        },
                    ),
            ),
        )
    }

@OptIn(ExperimentalCoroutinesApi::class)
class RoutesViewModelTest {
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
        favourites: FavouritesRepository = FakeFavouritesRepository(),
        lastViewedRoute: LastViewedRouteRepository = FakeLastViewedRouteRepository(),
    ): RoutesViewModel {
        val departures = FakeDeparturesRepository()
        val trackedTrips = FakeTrackedTripRepository()
        val saveLastViewedRoute = SaveLastViewedRouteUseCase(lastViewedRoute)
        return RoutesViewModel(
            getRouteDetail = GetRouteDetailUseCase(catalogue),
            getRouteShape = GetRouteShapeUseCase(catalogue),
            getRouteBusPositions = GetRouteBusPositionsUseCase(vehicles),
            getStopEtas = GetStopEtasUseCase(),
            getStopDepartures = GetStopDeparturesUseCase(departures),
            getLastViewedRoute = GetLastViewedRouteUseCase(lastViewedRoute),
            saveLastViewedRoute = saveLastViewedRoute,
            getRoutes = GetRoutesUseCase(catalogue),
            getFavourites = GetFavouriteStopsUseCase(favourites),
            toggleFavouriteUseCase = ToggleFavouriteUseCase(favourites),
            getTrackedTrip = GetTrackedTripUseCase(trackedTrips),
            updateTrackedTrip = UpdateTrackedTripUseCase(trackedTrips),
            startTracking = StartTrackingUseCase(trackedTrips, saveLastViewedRoute),
        )
    }

    @Test
    fun `given no last viewed route when the routes screen opens then the catalogue is listed and no route is selected`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel = createViewModel()

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.routes.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(listOf("4", ROUTE), state.routes.map { it.code })
                assertNull(state.selectedRouteNum)
                assertEquals(0, state.directionCount)
                assertTrue(state.routeStopRows.isEmpty())
                assertTrue(state.mapStops.isEmpty())
                assertTrue(state.busPositions.isEmpty())
                assertFalse(state.busFeedStale)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a last viewed route when the screen opens then its timeline and its buses are loaded`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    vehicles = FakeVehiclesRepository(routePositions),
                    lastViewedRoute = FakeLastViewedRouteRepository(ROUTE),
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.mapStops.isEmpty() || state.busPositions.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(ROUTE, state.selectedRouteNum)
                assertEquals(2, state.directionCount)
                assertEquals(listOf("Centro", "Cortijo"), state.directions)
                assertEquals(listOf("R1-1", "R1-2", "R1-3"), state.mapStops.map { it.stopRef })
                assertEquals(
                    listOf("R1-1", "R1-2", "R1-3"),
                    state.routeStopRows.map { row -> row.stop.stopRef },
                )
                assertEquals(listOf("trip-1"), state.busPositions.map { it.tripDuid })
                assertEquals(listOf("V1"), state.busPositions.map { it.vehicleId })
                assertFalse(state.busFeedStale)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the route detail cannot be read when the last viewed route is restored then the feed is stale and nothing is drawn`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    catalogue = FakeCatalogueRepository(failRouteDetail = true),
                    lastViewedRoute = FakeLastViewedRouteRepository(ROUTE),
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (!state.busFeedStale) {
                    state = awaitItem()
                }

                assertEquals(ROUTE, state.selectedRouteNum)
                assertEquals(0, state.directionCount)
                assertTrue(state.mapStops.isEmpty())
                assertTrue(state.routeStopRows.isEmpty())
                assertTrue(state.busPositions.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a line loaded when another line is selected then the previous line's stops are gone before the new one loads`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val viewModel =
                createViewModel(
                    catalogue = OnlyOneLineCatalogue(routeDetail),
                    vehicles = FakeVehiclesRepository(routePositions),
                    lastViewedRoute = FakeLastViewedRouteRepository(ROUTE),
                )

            // When
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.mapStops.isEmpty()) {
                    state = awaitItem()
                }
                viewModel.selectRoute("4")

                // Then
                state = awaitItem()
                while (state.selectedRouteNum != "4") {
                    state = awaitItem()
                }

                assertTrue(state.mapStops.isEmpty())
                assertTrue(state.routeStopRows.isEmpty())
                assertTrue(state.busPositions.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class OnlyOneLineCatalogue(
    private val detail: RouteDetail,
) : CatalogueRepository by FakeCatalogueRepository(routes = routes, detail = detail) {
    override suspend fun getStopsForRoute(routeNum: String): List<List<Stop>> = if (routeNum == ROUTE) detail.stops else emptyList()
}
