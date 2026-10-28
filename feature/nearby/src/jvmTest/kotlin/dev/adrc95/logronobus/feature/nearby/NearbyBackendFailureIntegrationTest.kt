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

import dev.adrc95.logronobus.core.data.repository.LocationRepositoryImpl
import dev.adrc95.logronobus.core.device.JvmLocationProvider
import dev.adrc95.logronobus.core.domain.usecase.GetCurrentLocationUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNearbyStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetWalkDistancesUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.failingBackend
import dev.adrc95.logronobus.core.testing.nonsenseBackend
import dev.adrc95.logronobus.feature.nearby.model.NearbyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyBackendFailureIntegrationTest {
    @BeforeTest
    fun setup() = Dispatchers.setMain(Dispatchers.Default)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `given an outage when nearby stops are asked for then it reports rather than crashes`() {
        runTest {
            val bus = TestBus(injectedHttpClient = failingBackend())
            val nearby = nearbyOn(bus)
            val watching = launch { nearby.uiState.collect {} }
            waitForRetries()
            nearby.reload()
            waitForRetries()
            assertEquals(NearbyState.Unavailable, nearby.uiState.value.nearbyState)
            watching.cancel()
        }
    }

    @Test
    fun `given a backend answering nonsense when nearby stops are asked for then it is survivable too`() {
        runTest {
            val repository = TestBus(injectedHttpClient = nonsenseBackend())
            val nearby = nearbyOn(repository)
            val watching = launch { nearby.uiState.collect {} }
            nearby.reload()
            waitForRetries()
            watching.cancel()
        }
    }

    private fun nearbyOn(bus: TestBus) =
        NearbyViewModel(
            GetNearbyStopsUseCase(GetStopsUseCase(bus.catalogue), GetWalkDistancesUseCase(bus.walking)),
            GetCurrentLocationUseCase(LocationRepositoryImpl(JvmLocationProvider())),
            GetFavouriteStopsUseCase(bus.favourites),
            ToggleFavouriteUseCase(bus.favourites),
            GetRoutesUseCase(bus.catalogue),
        )

    private suspend fun waitForRetries() = withContext(Dispatchers.Default) { delay(3_000) }
}
