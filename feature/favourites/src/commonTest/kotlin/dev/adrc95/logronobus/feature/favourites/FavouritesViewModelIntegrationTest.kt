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
import dev.adrc95.logronobus.core.data.repository.TrackedTripRepositoryImpl
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
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.stubbedBackend
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavouritesViewModelIntegrationTest {
    private val mainDispatcherRule = MainDispatcherRule()
    private val repository = TestBus(injectedHttpClient = stubbedBackend())

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        runTest { repository.favourites.replaceFavouriteStops(emptyList()) }
        mainDispatcherRule.tearDown()
    }

    @Test
    fun viewModelMigratesExistingFavourites() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val oldFavourite =
                favouriteStop {
                    withStopRef("152")
                    withName("Plaza de la iglesia El Cortijo")
                    withStopId("")
                }
            repository.favourites.replaceFavouriteStops(listOf(oldFavourite))

            // When
            val favourites =
                FavouritesViewModel(
                    GetFavouriteStopsUseCase(repository.favourites),
                    SaveFavouriteStopsUseCase(repository.favourites),
                    AddFavouriteUseCase(repository.favourites),
                    RemoveFavouriteUseCase(repository.favourites),
                    GetStopDeparturesUseCase(repository.departures),
                    GetStopLineStartsUseCase(repository.departures),
                    GetStopLineLabelsUseCase(repository.catalogue),
                    GetStopsUseCase(repository.catalogue),
                    GetStopDirectionsUseCase(repository.catalogue),
                    GetRoutesUseCase(repository.catalogue),
                    StartTrackingUseCase(
                        TrackedTripRepositoryImpl(),
                        SaveLastViewedRouteUseCase(repository.lastViewedRoute),
                    ),
                    SearchStopsUseCase(GetStopsUseCase(repository.catalogue)),
                ).also { mainDispatcherRule.track(it.viewModelScope) }

            // Then
            favourites.uiState.test {
                var state = awaitItem()
                while (state.cards.none { it.stopId == "152" }) {
                    state = awaitItem()
                }
                assertEquals("152", state.cards.first().stopId)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
