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

package dev.adrc95.logronobus.feature.stops

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import dev.adrc95.logronobus.core.data.repository.TrackedTripRepositoryImpl
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineLabelsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineStartsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.stubbedBackend
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StopsSheetViewModelIntegrationTest {
    private val mainDispatcherRule = MainDispatcherRule()
    private val backend = TestBus(injectedHttpClient = stubbedBackend())

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    @Test
    fun `given the stubbed backend when the sheet opens then it shows that stop and its times`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val sheet =
                StopsSheetViewModel(
                    StopSheetNavKey("152"),
                    GetStopUseCase(backend.catalogue),
                    GetStopDeparturesUseCase(backend.departures),
                    GetStopLineStartsUseCase(backend.departures),
                    GetStopLineLabelsUseCase(backend.catalogue),
                    GetRoutesUseCase(backend.catalogue),
                    GetFavouriteStopsUseCase(backend.favourites),
                    ToggleFavouriteUseCase(backend.favourites),
                    StartTrackingUseCase(
                        TrackedTripRepositoryImpl(),
                        SaveLastViewedRouteUseCase(backend.lastViewedRoute),
                    ),
                ).also { mainDispatcherRule.track(it.viewModelScope) }

            // When
            var state = sheet.uiState.value
            sheet.uiState.test {
                state = awaitItem()
                while (state?.rows.isNullOrEmpty()) {
                    state = awaitItem()
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            assertEquals("Plaza de la iglesia El Cortijo", state?.title)
            assertTrue(state?.rows?.isNotEmpty() == true, "the stub publishes arrivals for stop 152")
        }
}
