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

import dev.adrc95.logronobus.core.testing.EMPTY_FEEDS
import dev.adrc95.logronobus.core.testing.TestBus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class StopLineStartsArrivingBusIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val now = Instant.parse("2026-10-16T14:31:00Z")

    private val stopCatalogue = """{"result":{"stops":[
        {"id":5,"name":"Artesanos","lat":42.461701,"lng":-2.40653,"status":1,"lines":[2]},
        {"id":56,"name":"Manresa","lat":42.4745,"lng":-2.4605,"status":1,"lines":[2]}]}}"""

    private val lines = """{"result":{"lines":[
        {"id":2,"name":"2-YAGÜE-VAREA","color":"rgba(0,153,68,1)","status":1,
         "stops":{"asc":[{"id":56,"name":"Manresa","lat":42.4745,"lng":-2.4605,"status":1,"onwardShape":[]},
                         {"id":5,"name":"Artesanos","lat":42.461701,"lng":-2.40653,"status":1,"onwardShape":[]}],
                  "desc":[{"id":5,"name":"Artesanos","lat":42.461701,"lng":-2.40653,"status":1,"onwardShape":[]},
                          {"id":56,"name":"Manresa","lat":42.4745,"lng":-2.4605,"status":1,"onwardShape":[]}]}}]}}"""

    private fun arrival(expected: String) =
        """{"result":{"arrivals":[
        {"lineRef":"2","directionRef":"320","journeyCode":"","vehicleRef":"53","stopPointRef":"5","order":2,
         "aimedArrivalTime":"$expected","expectedArrivalTime":"$expected","aimedDepartureTime":"",
         "expectedDepartureTime":"","arrivalStatus":"NO_REPORT","departureStatus":"",
         "cancellation":false,"predictionInaccurate":false,"delaySeconds":null}]}}"""

    private fun pass(aimed: String) =
        """{"lineRef":"2","stopPointRef":"5","journeyCode":"1","order":2,
        "aimedArrivalTime":"$aimed","aimedDepartureTime":"$aimed"}"""

    private fun passes(vararg aimed: String) = """{"result":{"passes":[${aimed.joinToString(",") { pass(it) }}]}}"""

    private fun repository(
        arrivals: String,
        passes: String,
        requests: MutableList<String> = mutableListOf(),
    ) = TestBus(
        HttpClient(
            MockEngine { request ->
                val path = request.url.encodedPath
                requests += path
                val body =
                    when {
                        path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                        path.endsWith("/api/linesDiscovery/lines") -> lines
                        path.endsWith("/api/estimatedTimetable/byStop/5") -> arrivals
                        path.endsWith("/api/productionTimetable/byStop/5") -> passes
                        else -> EMPTY_FEEDS
                    }
                respond(body, HttpStatusCode.OK, jsonHeaders)
            },
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            expectSuccess = true
        },
    )

    @Test
    fun `given a bus the feed dropped while arriving when rows are built then it is still the row`() =
        runTest {
            val rows =
                repository(
                    arrivals = arrival("2026-10-16T17:15:00+02:00"),
                    passes = passes("2026-10-16T16:40:00+02:00", "2026-10-16T17:12:00+02:00"),
                ).departures.getStopLineStarts("5", now = now)

            assertEquals(1, rows.size)
            assertEquals(
                Instant.parse("2026-10-16T16:40:00+02:00"),
                rows.single().departTimestamp,
                "the bus arriving at the stop, not the one after it",
            )
        }

    @Test
    fun `given the feed and the timetable on the same bus when rows are built then the feed's prediction is kept`() =
        runTest {
            val rows =
                repository(
                    arrivals = arrival("2026-10-16T16:45:00+02:00"),
                    passes = passes("2026-10-16T16:42:00+02:00", "2026-10-16T17:12:00+02:00"),
                ).departures.getStopLineStarts("5", now = now)

            assertEquals(Instant.parse("2026-10-16T16:45:00+02:00"), rows.single().departTimestamp)
        }

    @Test
    fun `given a bus close by in the feed when rows are built then the timetable is not asked`() =
        runTest {
            val requests = mutableListOf<String>()
            val rows =
                repository(
                    arrivals = arrival("2026-10-16T16:35:00+02:00"),
                    passes = passes("2026-10-16T16:34:00+02:00"),
                    requests = requests,
                ).departures.getStopLineStarts("5", now = now)

            assertEquals(Instant.parse("2026-10-16T16:35:00+02:00"), rows.single().departTimestamp)
            assertTrue(
                requests.none { it.endsWith("/api/productionTimetable/byStop/5") },
                "no timetable request when the feed already has the next bus: $requests",
            )
        }
}
