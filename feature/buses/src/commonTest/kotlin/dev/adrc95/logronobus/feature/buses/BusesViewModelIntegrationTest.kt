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

package dev.adrc95.logronobus.feature.buses

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.usecase.GetBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.stubbedBackend
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BusesViewModelIntegrationTest {
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
    fun `given the stubbed backend when the screen loads then it has the stops of the network`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val buses =
                BusesViewModel(
                    GetBusPositionsUseCase(backend.vehicles),
                    GetStopsUseCase(backend.catalogue),
                ).also { mainDispatcherRule.track(it.viewModelScope) }

            // When
            var state = buses.uiState.value
            buses.uiState.test {
                state = awaitItem()
                while (state.stops.isEmpty()) {
                    state = awaitItem()
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            assertEquals(2, state.stops.size, "the catalogue answers with the two stops")
            assertTrue(state.fleet.buses.isEmpty(), "the stub reports no vehicles")
        }
}
