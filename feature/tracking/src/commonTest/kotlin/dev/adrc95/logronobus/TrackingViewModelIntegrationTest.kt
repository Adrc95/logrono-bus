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

package dev.adrc95.logronobus

import androidx.lifecycle.viewModelScope
import dev.adrc95.logronobus.core.data.repository.TrackedTripRepositoryImpl
import dev.adrc95.logronobus.core.domain.usecase.GetLineArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNextBusAtStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPinnedArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteShapeUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackedTripUseCase
import dev.adrc95.logronobus.core.domain.usecase.UpdateTrackedTripUseCase
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.testing.EMPTY_FEEDS
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.stubbedBackend
import dev.adrc95.logronobus.feature.tracking.TrackingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class TrackingViewModelIntegrationTest {
    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    private val row =
        departure {
            withDisplayName("Varea")
            withTimetableId("2")
            withDepartTimestamp(Instant.parse("2026-10-15T13:40:00+02:00"))
            withDelaySeconds(0)
            withTripId("76")
            withVehicleId("76")
            withDirectionIndex(0)
        }

    private fun tracking(): TrackingViewModel {
        val bus =
            TestBus(
                HttpClient(
                    MockEngine {
                        respond(
                            content = EMPTY_FEEDS,
                            status = HttpStatusCode.OK,
                            headers =
                                headersOf(
                                    HttpHeaders.ContentType,
                                    ContentType.Application.Json.toString(),
                                ),
                        )
                    },
                ) {
                    install(ContentNegotiation) {
                        json(
                            kotlinx.serialization.json.Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                    expectSuccess = true
                },
            )
        return TrackingViewModel(
            GetRouteShapeUseCase(bus.catalogue),
            GetRouteBusPositionsUseCase(bus.vehicles),
            GetRouteDetailUseCase(bus.catalogue),
            GetRoutesUseCase(bus.catalogue),
            GetNextBusAtStopUseCase(bus.departures),
            GetTrackedTripUseCase(TrackedTripRepositoryImpl()),
            GetPinnedArrivalsUseCase(GetLineArrivalsUseCase(bus.departures)),
            UpdateTrackedTripUseCase(TrackedTripRepositoryImpl()),
        ).also { mainDispatcherRule.track(it.viewModelScope) }
    }

    @Test
    fun `given a bus followed before the screen opens when the view model is created then it adopts the trip from the repository`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val bus = TestBus(injectedHttpClient = stubbedBackend())
            val tracked = TrackedTripRepositoryImpl()
            val followed =
                departure {
                    withTripId("trip-9")
                    withTimetableId("10")
                }
            tracked.update(TrackedTrip(tripId = followed.tripId, stopRef = "76", departure = followed))

            // When
            val tracking =
                TrackingViewModel(
                    GetRouteShapeUseCase(bus.catalogue),
                    GetRouteBusPositionsUseCase(bus.vehicles),
                    GetRouteDetailUseCase(bus.catalogue),
                    GetRoutesUseCase(bus.catalogue),
                    GetNextBusAtStopUseCase(bus.departures),
                    GetTrackedTripUseCase(tracked),
                    GetPinnedArrivalsUseCase(GetLineArrivalsUseCase(bus.departures)),
                    UpdateTrackedTripUseCase(tracked),
                ).also { mainDispatcherRule.track(it.viewModelScope) }

            // Then
            val watching = launch { tracking.uiState.collect {} }
            delay(500)
            assertEquals("trip-9", tracking.uiState.value.tracking.trackedTripId)
            assertEquals("76", tracking.uiState.value.tracking.trackedStopRef)
            assertEquals(
                "10",
                tracking.uiState.value.tracking.departure
                    ?.timetableId,
            )
            watching.cancel()
        }

    @Test
    fun `given a finished trip when the same bus moves on then it stays finished`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val tracking = tracking()
            val watching = launch { tracking.uiState.collect {} }
            delay(100)
            tracking.setTrackedDeparture(row, stopRef = "76")

            // When
            tracking.markTripCompleted()
            delay(200)
            val finished = tracking.uiState.value.tracking
            tracking.updateDeparture(row.copy(directionIndex = 1, displayName = "Yagüe"))
            delay(100)

            // Then
            assertTrue(finished.completed, "standing at the terminus ends the trip")
            assertTrue(finished.stopEtas.isEmpty(), "no times are kept for a trip that has ended")
            assertTrue(
                tracking.uiState.value.tracking.completed,
                "the same vehicle heading the other way is another trip, and this one stays closed",
            )
            assertTrue(
                tracking.uiState.value.tracking.stopEtas
                    .isEmpty(),
                "and no times come back either",
            )
            watching.cancel()
        }

    @Test
    fun `given a finished trip when a departure is picked then a fresh one starts`() =
        mainDispatcherRule.runViewModelTest {
            // Given
            val tracking = tracking()
            val watching = launch { tracking.uiState.collect {} }
            delay(100)
            tracking.setTrackedDeparture(row, stopRef = "76")
            tracking.markTripCompleted()
            delay(100)

            // When
            tracking.setTrackedDeparture(row.copy(tripId = "80", vehicleId = "80"), stopRef = "76")
            delay(100)

            // Then
            assertFalse(tracking.uiState.value.tracking.completed, "a new ride is not finished before it starts")
            watching.cancel()
        }
}
