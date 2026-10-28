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

import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.builder.geoPoint
import dev.adrc95.logronobus.core.testing.builder.walkDestination
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveIntegrationTest {
    @Test
    fun `given the real API when it is asked then it answers with the data the app needs`() =
        runTest {
            org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")

            val repository = TestBus()

            val routes = repository.catalogue.getRoutes()
            assertTrue(routes.isNotEmpty(), "the line catalogue answers")
            assertTrue(
                routes.keys.containsAll(listOf("6", "B1")),
                "daytime and night lines: $routes",
            )
            assertTrue(
                routes.values.all { it.color?.startsWith("rgba(") == true },
                "every line carries its colour",
            )
            val stops = repository.catalogue.getStops()
            assertTrue(
                stops.size > 200,
                "the stop catalogue answers (254 at the last count), got ${stops.size}",
            )
            val olimpia = stops.first { it.stopRef == "61" }
            assertEquals(
                olimpia.routes?.isNotEmpty(),
                true,
                "and each stop lists its lines as visible codes: ${olimpia.routes}",
            )
            val departures = repository.departures.getStopDeparturesWithLive("61").times
            println(
                "live: ${departures.size} departures at Olimpia (61): " +
                    departures.joinToString { "${it.timetableId}→${it.displayName.ifBlank { "?" }} @${it.departTimestamp} (${it.delaySeconds}s)" },
            )
            assertTrue(departures.isNotEmpty(), "the stop has departures right now")
            assertEquals(
                departures.sortedBy { it.departTimestamp },
                departures,
                "rows come in the order they leave: $departures",
            )
            val positions = repository.vehicles.getBusPositions()
            println("live: ${positions.all.size} buses reporting right now, stale=${positions.stale}")
            val routeStops = repository.catalogue.getStopsForRoute("6")
            assertEquals(2, routeStops.size, "line 6 has two directions")
            assertTrue(routeStops.all { it.isNotEmpty() })
            val walked =
                repository.walking.walkDistances(
                    origin =
                        geoPoint {
                            withLat(42.4653)
                            withLon(-2.4448)
                        },
                    destinations =
                        listOf(
                            walkDestination {
                                withId("611")
                                withLat(42.457834)
                                withLng(-2.441812)
                            },
                        ),
                )
            println("live: walking to Estación de Ferrocarril (611): ${walked.firstOrNull()}")
            assertTrue(walked.isNotEmpty(), "the walk-distance service answers")
            val fareCheck = repository.vehicles.getBusPositions(routeNum = "6")
            println("live: line 6 vehicles in that snapshot: ${fareCheck.forRoute("6").size}")
        }
}
