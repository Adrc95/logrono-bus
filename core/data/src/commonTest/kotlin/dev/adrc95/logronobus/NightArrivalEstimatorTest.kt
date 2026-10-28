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

import dev.adrc95.logronobus.core.common.exception.AppError
import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.data.vehicles.NightArrivalEstimator
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.StopPrediction
import dev.adrc95.logronobus.core.testing.fake.FakeDeparturesRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class NightArrivalEstimatorTest {
    private val now = Instant.parse("2026-10-24T05:26:00Z")

    @Test
    fun `given a night bus without a time when the timetable passes by then it gets the theoretical time`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Success(listOf(pass(order = 3, aimed = "2026-10-24T05:30:00Z"))),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1", delay = -60)),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertEquals(
                Instant.parse("2026-10-24T05:29:00Z"),
                buses
                    .forRoute("B1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
        }

    @Test
    fun `given a day bus without a time when the timetable passes by then nothing changes`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Success(listOf(pass(order = 3, aimed = "2026-10-24T05:30:00Z"))),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "1")),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertNull(
                buses
                    .forRoute("1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
            assertEquals(0, remote.passesCalls)
        }

    @Test
    fun `given a night bus with a live time when the timetable passes by then the live time wins`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Success(listOf(pass(order = 3, aimed = "2026-10-24T05:30:00Z"))),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1", time = Instant.parse("2026-10-24T05:25:00Z"))),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertEquals(
                Instant.parse("2026-10-24T05:25:00Z"),
                buses
                    .forRoute("B1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
            assertEquals(0, remote.passesCalls)
        }

    @Test
    fun `given a night bus when the timetable fails then it keeps no time`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Error(AppError.Connectivity),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1")),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertNull(
                buses
                    .forRoute("B1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
        }

    @Test
    fun `given passes in both directions when the order matches one then that pass is used`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer =
                        Result.Success(
                            listOf(
                                pass(order = 7, aimed = "2026-10-24T05:28:00Z"),
                                pass(order = 3, aimed = "2026-10-24T06:20:00Z"),
                            ),
                        ),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1", sequence = 3)),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertEquals(
                Instant.parse("2026-10-24T06:20:00Z"),
                buses
                    .forRoute("B1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
        }

    @Test
    fun `given a pass already gone when the timetable passes by then it keeps no time`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Success(listOf(pass(order = 3, aimed = "2026-10-24T05:20:00Z"))),
                )
            val estimator = NightArrivalEstimator(remote)

            val buses =
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1")),
                    now = now,
                    lineIdOf = lineIds,
                )

            assertNull(
                buses
                    .forRoute("B1")
                    .single()
                    .nextStops
                    ?.single()
                    ?.departureTimestamp,
            )
        }

    @Test
    fun `given the timetable already fetched when the fleet refreshes then the service is asked once`() =
        runTest {
            val remote =
                FakeDeparturesRemoteDataSource(
                    passesAnswer = Result.Success(listOf(pass(order = 3, aimed = "2026-10-24T05:30:00Z"))),
                )
            val estimator = NightArrivalEstimator(remote)

            repeat(3) {
                estimator.withTheoreticalArrivals(
                    buses = positions(nightBus(code = "B1")),
                    now = now,
                    lineIdOf = lineIds,
                )
            }

            assertEquals(1, remote.passesCalls)
        }

    private val lineIds: suspend (String) -> Int? = { code -> if (code == "B1") 31 else null }

    private fun nightBus(
        code: String,
        stopRef: String = "5",
        sequence: Int = 3,
        delay: Int? = null,
        time: Instant? = null,
    ) = BusLocation(
        latitude = 42.46,
        longitude = -2.44,
        modifiedTimestamp = Instant.parse("2026-10-24T05:25:00Z"),
        tripDuid = "7559",
        timetableId = code,
        nextStopRef = stopRef,
        nextStops =
            listOf(
                StopPrediction(
                    stopRef = stopRef,
                    stopSequence = sequence,
                    arrivalTimestamp = time,
                    departureTimestamp = time,
                    delay = delay,
                ),
            ),
    )

    private fun positions(vararg buses: BusLocation) = BusPositions(byRoute = buses.groupBy { it.timetableId.orEmpty() })

    private fun pass(
        order: Int,
        aimed: String,
    ) = Arrival(
        lineRef = "31",
        directionRef = "1",
        order = order,
        aimedArrival = Instant.parse(aimed),
        expectedArrival = null,
        aimedDeparture = null,
        expectedDeparture = null,
        vehicleRef = null,
        cancellation = false,
        predictionInaccurate = false,
        delaySeconds = null,
        arrivalStatus = "scheduled",
    )
}
