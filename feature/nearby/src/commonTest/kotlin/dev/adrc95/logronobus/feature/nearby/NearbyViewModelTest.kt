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

package dev.adrc95.logronobus.feature.nearby

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.domain.repository.LocationRepository
import dev.adrc95.logronobus.core.domain.repository.WalkingRepository
import dev.adrc95.logronobus.core.domain.usecase.GetCurrentLocationUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNearbyStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetWalkDistancesUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.LocationResult
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.WalkDestination
import dev.adrc95.logronobus.core.model.WalkDistance
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.builder.geoPoint
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import dev.adrc95.logronobus.core.testing.fake.FakeFavouritesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeLocationRepository
import dev.adrc95.logronobus.core.testing.fake.FakeWalkingRepository
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.feature.nearby.NearbyViewModel
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyViewModelTest {
    private val location =
        geoPoint {
            withLat(42.4655)
            withLon(-2.4443)
        }

    private val stops =
        listOf(
            stop {
                withStopRef("100")
                withStopId("100")
                withLongName("Murrieta")
                withShortName("Murrieta")
                withLatitude(42.4660)
                withLongitude(-2.4440)
                withRoutes(listOf("3", "1"))
            },
            stop {
                withStopRef("200")
                withStopId("200")
                withLongName("Varea")
                withShortName("Varea")
                withLatitude(42.4800)
                withLongitude(-2.4300)
                withRoutes(listOf("6"))
            },
        )

    private val catalogue = FakeCatalogueRepository(stops = stops)

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
        locationResult: LocationRepository,
        favouriteStops: List<FavouriteStop> = emptyList(),
        walkDistances: List<WalkDistance> = emptyList(),
    ): NearbyViewModel {
        val favourites = FakeFavouritesRepository(favouriteStops)
        return NearbyViewModel(
            getNearbyStops =
                GetNearbyStopsUseCase(
                    getStops = GetStopsUseCase(catalogue),
                    getWalkDistances = GetWalkDistancesUseCase(FakeWalkingRepository(walkDistances)),
                ),
            getCurrentLocation = GetCurrentLocationUseCase(locationResult),
            getFavourites = GetFavouriteStopsUseCase(favourites),
            toggleFavouriteUseCase = ToggleFavouriteUseCase(favourites),
            getRoutes = GetRoutesUseCase(catalogue),
        )
    }

    @Test
    fun `given a location when nearby loads then the closest stops are listed with their walking times`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    locationResult = FakeLocationRepository(LocationResult.Available(location)),
                    favouriteStops = listOf(favouriteStop { withStopRef("100") }),
                    walkDistances = listOf(WalkDistance(id = "100", distanceM = 180, durationMin = 2)),
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.nearbyState !is NearbyState.Located ||
                    state.nearby.stops.isEmpty() ||
                    state.favouriteRefs.isEmpty()
                ) {
                    state = awaitItem()
                }

                assertEquals(
                    LocationDisplayModel(latitude = 42.4655, longitude = -2.4443),
                    assertIs<NearbyState.Located>(state.nearbyState).location,
                )
                assertEquals(listOf("100", "200"), state.nearby.stops.map { it.stop.stopRef })
                assertEquals(listOf("Murrieta", "Varea"), state.nearby.stops.map { it.stop.name })
                assertEquals(
                    DistanceDisplayModel.Metres(180),
                    state.nearby.stops
                        .first()
                        .distance,
                )
                assertEquals(
                    2,
                    state.nearby.stops
                        .first()
                        .walkMinutes,
                )
                assertNull(
                    state.nearby.stops
                        .last()
                        .walkMinutes,
                )
                assertEquals(
                    listOf("1", "3"),
                    state.nearby.stops
                        .first()
                        .lineCodes,
                )
                assertTrue(
                    state.nearby.stops
                        .first()
                        .isFavourite,
                )
                assertFalse(
                    state.nearby.stops
                        .last()
                        .isFavourite,
                )
                assertFalse(state.nearby.isFallback)
                assertEquals(setOf("100"), state.favouriteRefs)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the location permission is denied when nearby loads then the denial is reported`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    locationResult = FakeLocationRepository(LocationResult.PermissionDenied),
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.nearbyState !is NearbyState.PermissionDenied) {
                    state = awaitItem()
                }

                assertEquals(NearbyState.PermissionDenied, state.nearbyState)
                assertTrue(state.nearby.stops.isEmpty())
                assertFalse(state.nearby.isFallback)
                assertTrue(state.favouriteRefs.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given no location when nearby loads then the stops around the centre are flagged as fallback`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    locationResult = FakeLocationRepository(LocationResult.Unavailable),
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.nearbyState !is NearbyState.Unavailable || state.nearby.stops.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(NearbyState.Unavailable, state.nearbyState)
                assertTrue(state.nearby.isFallback)
                assertEquals(listOf("100", "200"), state.nearby.stops.map { it.stop.stopRef })
                assertTrue(state.nearby.stops.all { it.walkMinutes == null })
                cancelAndIgnoreRemainingEvents()
            }
        }
}
