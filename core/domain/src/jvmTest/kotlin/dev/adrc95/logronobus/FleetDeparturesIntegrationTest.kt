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

import dev.adrc95.logronobus.core.domain.trip.headsign
import dev.adrc95.logronobus.core.testing.EMPTY_FEEDS
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.fleetForLine
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class FleetDeparturesIntegrationTest {
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

    private val fleet = """{"result":{"activities":[
        {"vehicleRef":"8348","lineRef":"2","directionRef":"Vuelta","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.462905,"longitude":-2.445732,"locationRecordedAtTime":"2026-10-16T14:30:40Z",
         "delaySeconds":37,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0005","nextStopExpectedArrivalTime":"2026-10-16T17:16:31Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:15:54Z"},
        {"vehicleRef":"7336","lineRef":"2","directionRef":"Vuelta","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.4629,"longitude":-2.4457,"locationRecordedAtTime":"2026-10-16T14:30:45Z",
         "delaySeconds":0,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0005","nextStopExpectedArrivalTime":"2026-10-16T17:45:00Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:45:00Z"},
        {"vehicleRef":"0219","lineRef":"2","directionRef":"Ida","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.4629,"longitude":-2.4457,"locationRecordedAtTime":"2026-10-16T14:30:50Z",
         "delaySeconds":-30,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0005","nextStopExpectedArrivalTime":"2026-10-16T17:12:30Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:13:00Z"},
        {"vehicleRef":"9999","lineRef":"2","directionRef":"Ida","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.4629,"longitude":-2.4457,"locationRecordedAtTime":"2026-10-16T13:40:00Z",
         "delaySeconds":10,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0005","nextStopExpectedArrivalTime":"2026-10-16T17:20:00Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:19:00Z"},
        {"vehicleRef":"8888","lineRef":"2","directionRef":"Vuelta","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.47,"longitude":-2.46,"locationRecordedAtTime":"2026-10-16T14:30:55Z",
         "delaySeconds":0,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0056","nextStopExpectedArrivalTime":"2026-10-16T17:20:00Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:20:00Z"}]}}"""

    private fun repository() =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> {
                                stopCatalogue
                            }

                            path.endsWith("/api/linesDiscovery/lines") -> {
                                lines
                            }

                            path.endsWith("/api/estimatedTimetable/byStop/5") -> {
                                arrivalsAt5
                            }

                            path.endsWith("/api/productionTimetable/byStop/5") -> {
                                passesAt5
                            }

                            path.endsWith("/api/vehicleMonitoring/all") -> {
                                fleet
                            }

                            path.contains("/api/vehicleMonitoring/byLine/") -> {
                                fleetForLine(fleet, path.substringAfterLast('/'))
                            }

                            else -> {
                                EMPTY_FEEDS
                            }
                        }
                    respond(body, HttpStatusCode.OK, jsonHeaders)
                },
            ) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                expectSuccess = true
            },
        )

    @Test
    fun `given the desc first stop when rows are linked then it gets the fleet's bus leaving it`() =
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
                2,
                departures.size,
                "the 17:15 departure and the fleet's 17:45 call, and nothing else",
            )
            val linked = departures.first()
            assertEquals(
                Instant.parse("2026-10-16T17:15:00+02:00"),
                linked.departTimestamp,
                "the row keeps the operator's own time",
            )
            assertEquals(
                "8348",
                linked.vehicleId,
                "the fleet's id: the one the map draws and 'Seguir bus' looks up",
            )
            assertEquals("8348", linked.tripId)
            assertEquals(37, linked.delaySeconds, "the fleet's live delay, not the timetable's silence")
            assertEquals(1, linked.directionIndex)
            assertNull(
                linked.arrivalStatus,
                "the feed called it 'scheduled'; the fleet has a bus on it",
            )

            val extra = departures[1]
            assertEquals("7336", extra.vehicleId, "a call no other source at the stop knows about")
            assertEquals(
                Instant.parse("2026-10-16T15:45:00Z"),
                extra.departTimestamp,
                "trap 16: 17:45 Madrid read as local",
            )
            assertEquals("Manresa", extra.headsign, "the desc direction's destination")
            assertEquals(0, extra.delaySeconds)

            assertTrue(extra.startsHere)
            assertTrue(
                departures.none { it.vehicleId == "9999" },
                "a 50-minute-old record is not a bus coming",
            )
            assertTrue(
                departures.none { it.vehicleId == "8888" },
                "a bus due at another stop is not due here",
            )
        }

    @Test
    fun `given the asc bus the feed tracks when it is mapped then it takes the fleet's vehicle id`() =
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

            assertEquals(
                1,
                departures.size,
                "the fleet's desc calls do not belong to this direction",
            )
            val row = departures.single()
            assertEquals(Instant.parse("2026-10-16T17:12:00+02:00"), row.departTimestamp)
            assertEquals(
                "0219",
                row.vehicleId,
                "the fleet's id, so the screen can find this very bus",
            )
            assertEquals("0219", row.tripId)
            assertEquals(-30, row.delaySeconds, "the fleet's live delay")
            assertEquals(0, row.directionIndex)
            assertTrue(row.terminatesHere, "the trip ends here: the feed published no departure")
        }

    @Test
    fun `given no fleet answer when the rows are built then they are what the feeds said`() =
        runTest {
            val repository =
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

            val departures =
                repository
                    .departures
                    .getStopDeparturesWithLive(
                        "5",
                        now = now,
                        lineFilter = "2",
                        directionFilter = 1,
                    ).times

            assertEquals(1, departures.size)
            assertEquals(Instant.parse("2026-10-16T17:15:00+02:00"), departures.single().departTimestamp)
            assertNull(departures.single().delaySeconds, "nobody at all is tracking it")
        }
}
