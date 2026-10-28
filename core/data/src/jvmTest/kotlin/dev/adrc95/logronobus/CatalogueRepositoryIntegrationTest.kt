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
import dev.adrc95.logronobus.core.testing.stubbedBackend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogueRepositoryIntegrationTest {
    private val repository = TestBus(injectedHttpClient = stubbedBackend())

    @Test
    fun getStopsReturnsValidStopIds() =
        runTest {
            val stops = repository.catalogue.getStops()
            assertTrue(stops.isNotEmpty())
            assertTrue(
                stops.any { it.stopId.isNotEmpty() && it.stopId != "0" },
                "Expected at least some stops to have a valid stopId",
            )

            val cortijo = stops.find { it.stopRef == "152" }
            assertEquals("152", cortijo?.stopId)
            assertEquals("Plaza de la iglesia El Cortijo", cortijo?.longName)
        }

    @Test
    fun getStopResolvesAStopThatIsOnlyInTheLine() =
        runTest {
            // Given
            val listed = repository.catalogue.getStops()
            val inLineOnly = listed.none { it.stopRef == "189" }

            // When
            val stop = repository.catalogue.getStop("189")

            // Then
            assertTrue(inLineOnly, "The fixture is meant to miss 189 from the stop list")
            assertEquals("Eliseo Pinedo", stop?.longName, "Provisional stops only live in their line")
        }

    @Test
    fun directionHeadsignsAlignWithStopLists() =
        runTest {
            val stopLists = repository.catalogue.getStopsForRoute("6")
            val headsigns = repository.catalogue.getDirectionHeadsigns("6")
            assertEquals(2, stopLists.size)
            assertEquals(
                stopLists.size,
                headsigns.size,
                "One headsign per direction, aligned by index",
            )

            stopLists.forEachIndexed { i, stops ->
                val destination = stops.last().longName
                assertEquals(
                    destination,
                    headsigns[i],
                    "A direction's label is its last stop, not its first",
                )
            }
        }
}
