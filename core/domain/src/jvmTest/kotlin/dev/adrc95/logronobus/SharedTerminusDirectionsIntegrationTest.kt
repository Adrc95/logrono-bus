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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SharedTerminusDirectionsIntegrationTest {
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

    private val arrivalsAt5 = """{"result":{"arrivals":[
        {"lineRef":"2","directionRef":"320","journeyCode":"12308","vehicleRef":"53","stopPointRef":"5","order":2,
         "aimedArrivalTime":"2026-10-16T17:12:00+02:00","expectedArrivalTime":"2026-10-16T17:12:00+02:00",
         "aimedDepartureTime":"","expectedDepartureTime":"",
         "arrivalStatus":"ARRIVING","departureStatus":"","cancellation":false,"predictionInaccurate":false,"delaySeconds":0}]}}"""

    private val passesAt5 = """{"result":{"passes":[
        {"lineRef":"2","stopPointRef":"5","journeyCode":"12308","order":2,
         "aimedArrivalTime":"2026-10-16T17:12:00+02:00","aimedDepartureTime":"2026-10-16T17:12:00+02:00"},
        {"lineRef":"2","stopPointRef":"5","journeyCode":"12220","order":1,
         "aimedArrivalTime":"2026-10-16T17:15:00+02:00","aimedDepartureTime":"2026-10-16T17:15:00+02:00"}]}}"""

    private fun repository() =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                            path.endsWith("/api/linesDiscovery/lines") -> lines
                            path.endsWith("/api/estimatedTimetable/byStop/5") -> arrivalsAt5
                            path.endsWith("/api/productionTimetable/byStop/5") -> passesAt5
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
    fun `given a shared terminus when the desc stop is read then the departure leaving it is shown`() =
        runTest {
            val departures =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "5",
                        now = now,
                        lineFilter = "2",
                        directionFilter = 1,
                    ).times

            assertEquals(
                1,
                departures.size,
                "the desc bus leaving at 17:15 is the one that belongs here",
            )
            val row = departures.single()
            assertEquals("2", row.timetableId)
            assertEquals(1, row.directionIndex, "the desc direction — the one being browsed")
            assertEquals(Instant.parse("2026-10-16T17:15:00+02:00"), row.departTimestamp)
            assertNull(row.delaySeconds, "nobody is tracking it yet: the timetable, not a bus")
            assertTrue(!row.terminatesHere, "the trip starts here, it does not end here")
        }

    @Test
    fun `given a shared terminus when the asc last stop is read then the bus ending there is shown`() =
        runTest {
            val departures =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "5",
                        now = now,
                        lineFilter = "2",
                        directionFilter = 0,
                    ).times

            assertEquals(1, departures.size)
            val row = departures.single()
            assertEquals(0, row.directionIndex, "the asc direction, the one that ends here")
            assertEquals(Instant.parse("2026-10-16T17:12:00+02:00"), row.departTimestamp)
            assertTrue(row.terminatesHere, "the feed publishes no departure for it: the trip is over")
        }
}
