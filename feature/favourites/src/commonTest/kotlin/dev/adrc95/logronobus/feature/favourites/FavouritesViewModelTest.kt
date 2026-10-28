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

package dev.adrc95.logronobus.feature.favourites

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.domain.repository.TrackedTripRepository
import dev.adrc95.logronobus.core.domain.usecase.AddFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDirectionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineLabelsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineStartsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.RemoveFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.SearchStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import dev.adrc95.logronobus.core.testing.fake.FakeDeparturesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeFavouritesRepository
import dev.adrc95.logronobus.core.testing.fake.FakeLastViewedRouteRepository
import dev.adrc95.logronobus.core.testing.fake.FakeTrackedTripRepository
import dev.adrc95.logronobus.feature.favourites.FavouritesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {
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
        favourites: FakeFavouritesRepository = FakeFavouritesRepository(),
        catalogue: FakeCatalogueRepository = FakeCatalogueRepository(),
        departures: FakeDeparturesRepository = FakeDeparturesRepository(),
    ) = FavouritesViewModel(
        GetFavouriteStopsUseCase(favourites),
        SaveFavouriteStopsUseCase(favourites),
        AddFavouriteUseCase(favourites),
        RemoveFavouriteUseCase(favourites),
        GetStopDeparturesUseCase(departures),
        GetStopLineStartsUseCase(departures),
        GetStopLineLabelsUseCase(catalogue),
        GetStopsUseCase(catalogue),
        GetStopDirectionsUseCase(catalogue),
        GetRoutesUseCase(catalogue),
        StartTrackingUseCase(
            FakeTrackedTripRepository(),
            SaveLastViewedRouteUseCase(FakeLastViewedRouteRepository()),
        ),
        SearchStopsUseCase(GetStopsUseCase(catalogue)),
    ).also { mainDispatcherRule.track(it.viewModelScope) }

    @Test
    fun `given no stored favourites when the screen opens then there are no cards and it is not loading`() =
        mainDispatcherRule.runViewModelTest {
            // When
            val model = createViewModel()

            // Then
            model.uiState.test {
                val state = awaitItem()
                assertTrue(state.cards.isEmpty())
                assertFalse(state.isLoading)
                assertFalse(state.isSearching)
                assertTrue(state.searchResults.isEmpty())
                assertNull(state.updatedMs)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a stored favourite when the departures arrive then the card shows the catalogue name and its rows`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val favourites =
                FakeFavouritesRepository(
                    listOf(
                        favouriteStop {
                            withStopRef("360")
                            withName("Plaza guardada")
                            withStopId("0")
                        },
                    ),
                )
            val catalogue =
                FakeCatalogueRepository(
                    stops =
                        listOf(
                            stop {
                                withStopRef("360")
                                withStopId("12")
                                withLongName("Vitoria")
                                withShortName("Vitoria")
                            },
                        ),
                )
            val departures =
                FakeDeparturesRepository(
                    times = mapOf("360" to listOf(departure { withTimetableId("4") })),
                )
            // When
            val model =
                createViewModel(
                    favourites = favourites,
                    catalogue = catalogue,
                    departures = departures,
                )

            // Then
            model.uiState.test {
                var state = awaitItem()
                while (state.cards.none { it.rows.isNotEmpty() && it.name == "Vitoria" }) {
                    state = awaitItem()
                }
                val card = state.cards.single()
                assertEquals("360", card.stopRef)
                assertEquals("Vitoria", card.name)
                assertEquals("12", card.stopId)
                assertEquals(1, card.rows.size)
                assertEquals("4", card.rows.single().lineCode)
                assertTrue(card.hasLoadedDepartures)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the departures fail when the favourites load then the card is kept with no rows`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val favourites =
                FakeFavouritesRepository(
                    listOf(
                        favouriteStop {
                            withStopRef("360")
                            withName("Vitoria")
                            withStopId("12")
                        },
                    ),
                )
            // When
            val model =
                createViewModel(
                    favourites = favourites,
                    departures = FakeDeparturesRepository(failing = true),
                )

            // Then
            model.uiState.test {
                var state = awaitItem()
                while (state.cards.none { it.hasLoadedDepartures } || state.isLoading) {
                    state = awaitItem()
                }
                val card = state.cards.single()
                assertEquals("Vitoria", card.name)
                assertTrue(card.rows.isEmpty())
                assertTrue(card.hasLoadedDepartures)
                assertFalse(state.isLoading)
                assertNull(state.updatedMs)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
