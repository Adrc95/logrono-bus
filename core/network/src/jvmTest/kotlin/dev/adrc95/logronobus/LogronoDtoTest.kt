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

import dev.adrc95.logronobus.core.network.NetworkConstants.NO_LINE
import dev.adrc95.logronobus.core.network.dto.ApiFleetResponse
import dev.adrc95.logronobus.core.network.dto.ApiLinesResponse
import dev.adrc95.logronobus.core.network.dto.ApiStopsResponse
import dev.adrc95.logronobus.core.network.dto.ApiTimetableResponse
import dev.adrc95.logronobus.core.network.dto.ApiWalkResponse
import dev.adrc95.logronobus.core.network.dto.StopDto
import dev.adrc95.logronobus.core.network.mapper.visibleCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogronoDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "missing fixture $name"
        }.bufferedReader().readText()

    @Test
    fun `given the stops payload when it is decoded then the catalogue comes out`() {
        val stops = json.decodeFromString<ApiStopsResponse>(fixture("stops.json")).result.stops
        assertTrue(stops.isNotEmpty(), "254 stops in the live catalogue")
        val vitoria = stops.first { it.name == "Vitoria" }

        assertTrue(
            vitoria.lines.containsAll(listOf(31, 10, 4)),
            "internal ids as published: $vitoria",
        )
        assertEquals(42.463968, vitoria.lat, 1e-6)
    }

    @Test
    fun `given the lines payload when it is decoded then both directions and their colours come out`() {
        val lines = json.decodeFromString<ApiLinesResponse>(fixture("lines.json")).result.lines
        assertEquals(listOf(6, 31), lines.map { it.id })
        val night = lines.first { it.id == 31 }
        assertEquals("B1", visibleCode(night.name))
        assertEquals("rgba(253,2,0,1)", night.color)
        assertTrue(night.stops.asc.isNotEmpty() && night.stops.desc.isNotEmpty())
        val first = night.stops.asc.first()
        assertTrue(
            first.onwardShape.isNotEmpty(),
            "each stop carries the road geometry to the next one",
        )
    }

    @Test
    fun `given an arrivals payload when it is decoded then the real-time fields come out`() {
        val arrivals =
            json
                .decodeFromString<ApiTimetableResponse>(fixture("estimatedTimetable.json"))
                .result.arrivals
        assertEquals(5, arrivals.size, "four from the captured body plus the small-hours trip")
        val first = arrivals.first()
        assertEquals("6", first.lineRef)
        assertEquals(11, first.order)
        assertEquals("2026-10-15T13:45:30+02:00", first.expectedArrivalTime)
        assertEquals(0, first.delaySeconds)
        assertTrue(!first.cancellation)
        val cancelled = arrivals.first { it.cancellation }
        assertEquals("10", cancelled.lineRef, "the API flags cancelled calls explicitly")
    }

    @Test
    fun `given a fleet payload when it is decoded then even the junk rows are read`() {
        val activities =
            json
                .decodeFromString<ApiFleetResponse>(fixture("vehicleMonitoring.json"))
                .result.activities
        assertEquals(9, activities.size)
        val junk = activities.first { it.latitude == 0.0 }
        assertEquals(
            "2026-02-23T10:00:00Z",
            junk.locationRecordedAtTime,
            "old records are mixed in (trap 11)",
        )
        val objectObject = activities.first { it.lineRef == NO_LINE }
        assertNull(visibleCode(objectObject.lineRef).takeIf { it.isNotBlank() })
    }

    @Test
    fun `given a walk payload when it is decoded then the distances come out`() {
        val items =
            json
                .decodeFromString<ApiWalkResponse>(
                    """
                    {"items":[{"id":611,"distanceM":1347,"durationSec":1034}]}
                    """.trimIndent(),
                ).items
        assertEquals(
            "611",
            items
                .single()
                .id
                ?.jsonPrimitive
                ?.content,
        )
        assertEquals(1347, items.single().distanceM)
        assertEquals(1034, items.single().durationSec)
    }

    @Test
    fun `given a payload with unknown fields when it is decoded then they are ignored`() {
        val stop =
            json.decodeFromString<StopDto>(
                """{"id":1,"name":"X","lat":1.0,"lng":2.0,"status":1,"lines":[],"somethingNew":42}""",
            )
        assertEquals("X", stop.name)
    }
}
