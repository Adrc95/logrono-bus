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

package dev.adrc95.logronobus.feature.fares

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.usecase.GetFareProductsUseCase
import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.fare
import dev.adrc95.logronobus.core.testing.fake.FakeFaresRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FaresViewModelTest {
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
        fares: List<Fare> = listOf(fare()),
    ) = FaresViewModel(
        getFareProducts =
            GetFareProductsUseCase(
                FakeFaresRepository(fares),
            ),
    )

    @Test
    fun `given a catalogue with one fare when the screen opens then the fare arrives and the loading is gone`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel = createViewModel()

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading, "the loading is gone once the fares arrive")
                assertEquals(1, state.fares.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an empty catalogue when the screen opens then there is no fare and no error`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel = createViewModel(fares = emptyList())

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertTrue(state.fares.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
