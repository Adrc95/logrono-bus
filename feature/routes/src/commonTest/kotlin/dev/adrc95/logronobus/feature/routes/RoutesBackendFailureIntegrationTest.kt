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

import dev.adrc95.logronobus.core.data.repository.TrackedTripRepositoryImpl
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
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.failingBackend
import dev.adrc95.logronobus.core.testing.nonsenseBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoutesBackendFailureIntegrationTest {
    @BeforeTest
    fun setup() = Dispatchers.setMain(Dispatchers.Default)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `given an outage when a stop's departures are asked for then they survive`() {
        runTest {
            val routes = routesOn(TestBus(injectedHttpClient = failingBackend()))
            routes.selectStop("8460B522331")
            waitForRetries()
            assertTrue(
                routes.uiState.value.routeStopRows
                    .isEmpty(),
            )
        }
    }

    @Test
    fun `given a backend answering nonsense when a stop's departures are asked for then it is survivable too`() {
        runTest {
            val repository = TestBus(injectedHttpClient = nonsenseBackend())
            routesOn(repository).selectRoute("401")
            waitForRetries()
        }
    }

    private fun routesOn(bus: TestBus): RoutesViewModel {
        val trackedTrip = TrackedTripRepositoryImpl()
        return RoutesViewModel(
            GetRouteDetailUseCase(bus.catalogue),
            GetRouteShapeUseCase(bus.catalogue),
            GetRouteBusPositionsUseCase(bus.vehicles),
            GetStopEtasUseCase(),
            GetStopDeparturesUseCase(bus.departures),
            GetLastViewedRouteUseCase(bus.lastViewedRoute),
            SaveLastViewedRouteUseCase(bus.lastViewedRoute),
            GetRoutesUseCase(bus.catalogue),
            GetFavouriteStopsUseCase(bus.favourites),
            ToggleFavouriteUseCase(bus.favourites),
            GetTrackedTripUseCase(trackedTrip),
            UpdateTrackedTripUseCase(trackedTrip),
            StartTrackingUseCase(trackedTrip, SaveLastViewedRouteUseCase(bus.lastViewedRoute)),
        )
    }

    private suspend fun waitForRetries() = withContext(Dispatchers.Default) { delay(3_000) }
}
