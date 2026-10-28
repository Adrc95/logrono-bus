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

class FleetLinkOrderIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val now = Instant.parse("2026-10-16T15:21:00Z")

    private val stopCatalogue = """{"result":{"stops":[
        {"id":10,"name":"Primera","lat":42.47,"lng":-2.46,"status":1,"lines":[9]},
        {"id":20,"name":"Segunda","lat":42.468,"lng":-2.455,"status":1,"lines":[9]},
        {"id":30,"name":"Tercera","lat":42.466,"lng":-2.45,"status":1,"lines":[9]},
        {"id":40,"name":"Cuarta","lat":42.464,"lng":-2.445,"status":1,"lines":[9]},
        {"id":50,"name":"Quinta","lat":42.462,"lng":-2.44,"status":1,"lines":[9]},
        {"id":60,"name":"Sexta","lat":42.46,"lng":-2.435,"status":1,"lines":[9]}]}}"""

    private fun lineStop(
        id: Int,
        name: String,
    ) = """{"id":$id,"name":"$name","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]}"""

    private val lines = """{"result":{"lines":[
        {"id":9,"name":"9-PRIMERA-SEXTA","color":"rgba(0,102,153,1)","status":1,
         "stops":{"asc":[${lineStop(10, "Primera")},${lineStop(20, "Segunda")},${
        lineStop(
            30,
            "Tercera",
        )
    },
                         ${lineStop(40, "Cuarta")},${lineStop(50, "Quinta")},${
        lineStop(
            60,
            "Sexta",
        )
    }],
                  "desc":[${lineStop(60, "Sexta")},${lineStop(50, "Quinta")},${
        lineStop(
            40,
            "Cuarta",
        )
    },
                          ${lineStop(30, "Tercera")},${lineStop(20, "Segunda")},${
        lineStop(
            10,
            "Primera",
        )
    }]}}]}}"""

    private fun arrival(
        journey: String,
        vehicle: String,
        time: String,
        status: String,
    ) = """
        {"lineRef":"9","directionRef":"","journeyCode":"$journey","vehicleRef":"$vehicle","stopPointRef":"40","order":4,
         "aimedArrivalTime":"$time","expectedArrivalTime":"$time",
         "aimedDepartureTime":"$time","expectedDepartureTime":"$time",
         "arrivalStatus":"$status","departureStatus":"$status","cancellation":false,
         "predictionInaccurate":false,"delaySeconds":0}"""

    private val arrivalsAt40 = """{"result":{"arrivals":[
        ${arrival("9001", "83", "2026-10-16T17:30:00+02:00", "NO_REPORT")},
        ${arrival("9002", "", "2026-10-16T17:45:00+02:00", "scheduled")},
        ${arrival("9003", "", "2026-10-16T18:10:00+02:00", "scheduled")}]}}"""

    private fun activity(
        vehicle: String,
        nextStop: String,
        expected: String,
        delay: Int,
    ) = """
        {"vehicleRef":"$vehicle","lineRef":"9","directionRef":"Ida","publishedLineName":"PRIMERA-SEXTA",
         "latitude":42.465,"longitude":-2.445,"locationRecordedAtTime":"2026-10-16T15:20:30Z",
         "delaySeconds":$delay,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"$nextStop","nextStopExpectedArrivalTime":"$expected",
         "nextStopAimedArrivalTime":"$expected"}"""

    private val fleet = """{"result":{"activities":[
        ${activity("7001", "0030", "2026-10-16T17:24:00Z", 45)},
        ${activity("7002", "0010", "2026-10-16T17:10:00Z", -20)},
        ${activity("7003", "0050", "2026-10-16T17:25:00Z", 0)}]}}"""

    private fun repository(fleetBody: String = fleet) =
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

                            path.endsWith("/api/estimatedTimetable/byStop/40") -> {
                                arrivalsAt40
                            }

                            path.endsWith("/api/vehicleMonitoring/all") -> {
                                fleetBody
                            }

                            path.contains("/api/vehicleMonitoring/byLine/") -> {
                                fleetForLine(fleetBody, path.substringAfterLast('/'))
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
    fun `given several buses behind the stop when rows are linked then the nearest takes the earliest row`() =
        runTest {
            val departures =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "40",
                        now = now,
                        lineFilter = "9",
                        directionFilter = 0,
                    ).times

            assertEquals(3, departures.size, "every row the feeds have, and no invented ones")

            val first = departures[0]
            assertEquals(
                Instant.parse("2026-10-16T17:30:00+02:00"),
                first.departTimestamp,
                "the row keeps the operator's time",
            )
            assertEquals(
                "7001",
                first.vehicleId,
                "the bus one stop behind it, not the one three stops away",
            )
            assertEquals("7001", first.tripId)
            assertEquals(
                45,
                first.delaySeconds,
                "the fleet's live delay replaces the feed's silence",
            )
            assertNull(
                first.arrivalStatus,
                "the arrivals feed's own numbering is replaced by a bus we can follow",
            )
            assertEquals("Sexta", first.headsign, "the asc direction's destination")

            val second = departures[1]
            assertEquals(Instant.parse("2026-10-16T17:45:00+02:00"), second.departTimestamp)
            assertEquals("7002", second.vehicleId, "the next one behind serves the row after it")
            assertEquals(-20, second.delaySeconds)

            val third = departures[2]
            assertEquals(Instant.parse("2026-10-16T18:10:00+02:00"), third.departTimestamp)
            assertTrue(
                third.vehicleId !in setOf("7001", "7002", "7003"),
                "with every reported bus spoken for, the last row is nobody's: ${third.vehicleId}",
            )
            assertEquals("scheduled", third.arrivalStatus, "and it stays what the feed said it was")
        }

    @Test
    fun `given a bus past the stop when rows are linked then it never takes one`() =
        runTest {
            val onlyPast =
                """{"result":{"activities":[${activity("7003", "0050", "2026-10-16T17:25:00Z", 0)}]}}"""

            val row =
                repository(fleetBody = onlyPast)
                    .departures
                    .getStopDeparturesWithLive(
                        "40",
                        now = now,
                        lineFilter = "9",
                        directionFilter = 0,
                    ).times
                    .first()

            assertEquals("83", row.vehicleId, "the arrivals feed's own id, left as it was")
            assertEquals("NO_REPORT", row.arrivalStatus, "and the row still reads as real time's own")
        }
}
