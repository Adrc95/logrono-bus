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

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.usecase.GetBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.builder.busLocation
import dev.adrc95.logronobus.core.testing.builder.busPositions
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import dev.adrc95.logronobus.core.testing.fake.FakeVehiclesRepository
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable_detail
import dev.adrc95.logronobus.core.ui.generated.resources.loading_live_buses
import dev.adrc95.logronobus.feature.buses.model.FleetStatus
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BusesViewModelTest {
    private val stops =
        listOf(
            stop {
                withStopRef("100")
                withStopId("100")
                withLongName("Murrieta")
                withShortName("Murrieta")
            },
            stop {
                withStopRef("200")
                withStopId("200")
                withLongName("Portales")
                withShortName("Portales")
            },
        )

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(vehicles: VehiclesRepository) =
        BusesViewModel(
            getBusPositions = GetBusPositionsUseCase(vehicles),
            getStops = GetStopsUseCase(FakeCatalogueRepository(stops = stops)),
        )

    @Test
    fun `given the live feed has not answered when the fleet screen opens then the loading status is kept`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel = createViewModel(FakeVehiclesRepository { awaitCancellation() })

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.stops.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(
                    FleetStatus.Loading(Res.string.loading_live_buses),
                    state.fleet.status,
                )
                assertTrue(state.fleet.buses.isEmpty())
                assertNull(state.fleet.updatedMs)
                assertEquals(listOf("100", "200"), state.stops.map { it.stopRef })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given positions and stops when the feed arrives then every bus is listed as running`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    FakeVehiclesRepository {
                        busPositions {
                            withByRoute(
                                mapOf(
                                    "4" to
                                        listOf(
                                            busLocation {
                                                withVehicleId("V1")
                                                withTripDuid("trip-1")
                                                withTimetableId("4")
                                                withHeadsign("El Cortijo")
                                            },
                                            busLocation {
                                                withVehicleId("V2")
                                                withTripDuid("trip-2")
                                                withTimetableId("6")
                                                withHeadsign("Centro")
                                            },
                                        ),
                                ),
                            )
                        }
                    },
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.fleet.buses.isEmpty() || state.stops.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(FleetStatus.Running, state.fleet.status)
                assertEquals(listOf("V1", "V2"), state.fleet.buses.map { it.vehicleId })
                assertEquals(listOf("4", "6"), state.fleet.buses.map { it.timetableId })
                assertNotNull(state.fleet.updatedMs)
                assertEquals(listOf("100", "200"), state.stops.map { it.stopRef })
                assertEquals(listOf("Murrieta", "Portales"), state.stops.map { it.name })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the positions request fails when the feed is read then the fleet is reported unavailable`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel =
                createViewModel(
                    FakeVehiclesRepository { throw IllegalStateException("the fleet feed is down") },
                )

            // Then
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.fleet.status !is FleetStatus.Unavailable || state.stops.isEmpty()) {
                    state = awaitItem()
                }

                assertEquals(
                    FleetStatus.Unavailable(
                        title = Res.string.live_feed_unavailable,
                        detail = Res.string.live_feed_unavailable_detail,
                    ),
                    state.fleet.status,
                )
                assertTrue(state.fleet.buses.isEmpty())
                assertNull(state.fleet.updatedMs)
                assertEquals(listOf("100", "200"), state.stops.map { it.stopRef })
                cancelAndIgnoreRemainingEvents()
            }
        }
}
