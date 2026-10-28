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

import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.testing.TestBus
import kotlinx.coroutines.test.runTest
import org.junit.Assume
import kotlin.test.Test
import kotlin.time.Instant

class LiveTrackingProbeIntegrationTest {
    @Test
    fun `given the tracking screen when it looks for followed buses then it finds the published ones`() =
        runTest {
            Assume.assumeTrue(System.getenv("LIVE_API") == "1")

            val repository = TestBus()
            val now = Instant.fromEpochMilliseconds(nowEpochMilliseconds())

            val fleet = repository.vehicles.getBusPositions(forceRefresh = true)
            println("live: ${fleet.all.size} buses reporting; lines with buses: ${fleet.byRoute.mapValues { it.value.size }}")

            var rows = 0
            var withVehicle = 0
            var inRouteFleet = 0
            val examples = mutableListOf<String>()

            for ((code, buses) in fleet.byRoute) {
                val stops =
                    repository.catalogue
                        .getStopsForRoute(code)
                        .flatten()
                        .distinctBy { it.stopRef }
                for (stop in stops
                    .filter { st -> buses.any { it.nextStopRef == st.stopRef } }
                    .take(2)) {
                    val departures =
                        repository.departures
                            .getStopDeparturesWithLive(
                                stop.stopRef,
                                lineFilter = code,
                                now = now,
                            ).times
                    departures.forEach { row ->
                        rows++

                        val found =
                            buses.any {
                                (row.vehicleId != null && it.vehicleId == row.vehicleId) || it.tripDuid == row.tripId
                            }
                        if (row.vehicleId != null) withVehicle++
                        if (found) inRouteFleet++
                        if (!found && examples.size < 12) {
                            examples += "line $code @${stop.stopRef} ${row.departTimestamp} " +
                                "vehicle=${row.vehicleId} trip=${row.tripId} arrivals=${row.arrivalStatus} " +
                                "fleetIds=${buses.mapNotNull { it.vehicleId }.take(6)}"
                        }
                    }
                }
            }

            println("live: rows=$rows withVehicleId=$withVehicle foundInRouteFleet=$inRouteFleet")
            examples.forEach { println("live: NOT FOUND $it") }

            for (stopRef in listOf("88", "157")) {
                val departures =
                    repository.departures.getStopDeparturesWithLive(stopRef, now = now).times
                println("live: favourites stop $stopRef -> ${departures.size} rows")
                departures.forEach { row ->
                    val lineBuses = fleet.byRoute[row.timetableId].orEmpty()
                    val found =
                        lineBuses.any {
                            (row.vehicleId != null && it.vehicleId == row.vehicleId) || it.tripDuid == row.tripId
                        }
                    println(
                        "live:   stop $stopRef line=${row.timetableId} dir=${row.directionIndex} " +
                            "vehicle=${row.vehicleId} trip=${row.tripId} at=${row.departTimestamp} " +
                            "status=${row.arrivalStatus} lineBuses=${lineBuses.mapNotNull { it.vehicleId }} " +
                            "found=$found",
                    )
                }
            }
        }
}
