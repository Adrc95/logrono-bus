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

import dev.adrc95.logronobus.core.model.Stop
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class StopLineListIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val now = Instant.parse("2026-10-15T11:00:00Z")

    private class Recorder {
        val urls = mutableListOf<String>()
    }

    private val stops =
        """
        {"result":{"stops":[
          {"id":40,"name":"La Cometa","lat":42.4664,"lng":-2.4585,"status":1,"lines":[3]},
          {"id":41,"name":"Vitoria","lat":42.4678,"lng":-2.4531,"status":1,"lines":[4]},
          {"id":157,"name":"Vitoria","lat":42.4678,"lng":-2.4531,"status":1,"lines":[31]}
        ]}}
        """.trimIndent()

    private fun lineStop(
        id: Int,
        name: String,
        lat: Double,
        lng: Double,
    ) = """{"id":$id,"name":"$name","lat":$lat,"lng":$lng,"status":1,"onwardShape":[]}"""

    private val lines =
        """
        {"result":{"lines":[
          {"id":3,"name":"3-EL CAMPILLO-VILLAMEDIANA","color":"rgba(1,2,3,1)","status":1,
           "stops":{"asc":[${lineStop(40, "La Cometa", 42.4664, -2.4585)},
                           ${lineStop(700, "Villamediana", 42.4550, -2.4460)},
                           ${lineStop(90, "El Campillo", 42.4600, -2.4520)}],
                    "desc":[${lineStop(90, "El Campillo", 42.4600, -2.4520)},
                            ${lineStop(700, "Villamediana", 42.4550, -2.4460)},
                            ${lineStop(40, "La Cometa", 42.4664, -2.4585)}]}},
          {"id":4,"name":"4-PALACIO DE CONGRESOS-PRADOVIEJOO","color":"rgba(1,2,3,1)","status":1,
           "stops":{"asc":[${lineStop(41, "Vitoria", 42.4678, -2.4531)},
                           ${lineStop(42, "Palacio de Congresos", 42.4683, -2.4309)}],
                    "desc":[${lineStop(42, "Palacio de Congresos", 42.4683, -2.4309)},
                            ${lineStop(41, "Vitoria", 42.4678, -2.4531)}]}},
          {"id":31,"name":"B1-VAREA-LA CAVA","color":"rgba(253,2,0,1)","status":1,
           "stops":{"asc":[${lineStop(157, "Vitoria", 42.4678, -2.4531)},
                           ${lineStop(5, "Pradoviejo", 42.4590, -2.4400)}],
                    "desc":[${lineStop(5, "Pradoviejo", 42.4590, -2.4400)},
                            ${lineStop(157, "Vitoria", 42.4678, -2.4531)}]}}
        ]}}
        """.trimIndent()

    private fun arrivals(
        stop: Int,
        aimed: String,
        expected: String,
    ) = """
        {"result":{"arrivals":[
          {"lineRef":"3","directionRef":"464","journeyCode":"7","vehicleRef":"80","stopPointRef":"$stop",
           "order":1,
           "aimedArrivalTime":"2026-10-15T$aimed+02:00","expectedArrivalTime":"2026-10-15T$expected+02:00",
           "aimedDepartureTime":"2026-10-15T$aimed+02:00","expectedDepartureTime":"2026-10-15T$expected+02:00",
           "arrivalStatus":"NO_REPORT","departureStatus":"NO_REPORT","cancellation":false,
           "predictionInaccurate":false,"delaySeconds":null}
        ]}}
        """.trimIndent()

    private fun passes(
        stop: Int,
        at: String,
    ) = """
        {"result":{"passes":[
          {"lineRef":"3","stopPointRef":"$stop","journeyCode":"7","order":1,
           "aimedArrivalTime":"2026-10-15T$at+02:00","aimedDepartureTime":"2026-10-15T$at+02:00"}
        ]}}
        """.trimIndent()

    private fun repository(
        overrides: Map<String, String> = emptyMap(),
        recorder: Recorder = Recorder(),
    ) = TestBus(
        injectedHttpClient =
            HttpClient(
                MockEngine { request ->
                    recorder.urls.add(request.url.toString())
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> {
                                stops
                            }

                            path.endsWith("/api/linesDiscovery/lines") -> {
                                lines
                            }

                            path.contains("/api/estimatedTimetable/byStop/") -> {
                                overrides["arrivals"] ?: """{"result":{"arrivals":[]}}"""
                            }

                            path.contains("/api/productionTimetable/byStop/") -> {
                                overrides["passes"] ?: """{"result":{"passes":[]}}"""
                            }

                            path.contains("/api/productionTimetable/byLine/") -> {
                                overrides["passesByLine"] ?: """{"result":{"passes":[]}}"""
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
    fun `given a line whose arrivals have gone when rows are built then it keeps its row from the timetable`() =
        runTest {
            val repository =
                repository(
                    overrides =
                        mapOf(
                            "arrivals" to arrivals(stop = 40, aimed = "12:50:00", expected = "12:58:00"),
                            "passes" to passes(stop = 40, at = "13:20:00"),
                        ),
                )

            val rows = repository.departures.getStopLineStarts("40", now = now)

            assertEquals(1, rows.size, "one row per line: $rows")
            assertEquals("3", rows.single().timetableId)
            assertEquals(Instant.parse("2026-10-15T13:20:00+02:00"), rows.single().departTimestamp)
            assertNull(rows.single().delaySeconds, "a timetable pass is not a tracked bus")
            assertNull(rows.single().vehicleId, "no bus to follow")
        }

    @Test
    fun `given a line with a live arrival when rows are built then it is listed once at the live time`() =
        runTest {
            val repository =
                repository(
                    overrides =
                        mapOf(
                            "arrivals" to arrivals(stop = 40, aimed = "13:02:00", expected = "13:05:00"),
                            "passes" to passes(stop = 40, at = "13:02:00"),
                        ),
                )

            val rows = repository.departures.getStopLineStarts("40", now = now)

            assertEquals(1, rows.size, "one row per line: $rows")
            assertEquals(Instant.parse("2026-10-15T13:05:00+02:00"), rows.single().departTimestamp)
            assertEquals(180, rows.single().delaySeconds, "the live delay is kept")
            assertEquals("80", rows.single().vehicleId, "the feed's own number, kept as the feed's")
        }

    @Test
    fun `given a stop the catalogue does not know when rows are built then the lines calling there are listed`() =
        runTest {
            val recorder = Recorder()
            val repository =
                repository(
                    overrides =
                        mapOf(
                            "arrivals" to
                                arrivals(
                                    stop = 700,
                                    aimed = "13:10:00",
                                    expected = "13:12:00",
                                ),
                        ),
                    recorder = recorder,
                )

            val rows = repository.departures.getStopLineStarts("700", now = now)

            assertEquals(1, rows.size, "the stop is in line 3's sequence: $rows")
            assertEquals("3", rows.single().timetableId)
            assertTrue(
                recorder.urls.any { it.contains("/api/estimatedTimetable/byStop/700?lines=3") },
                "the request carries the internal line id: ${recorder.urls}",
            )
        }

    @Test
    fun `given a line with nothing left today when rows are built then its next service start is listed`() =
        runTest {
            val repository =
                repository(
                    overrides = mapOf("passesByLine" to """{"result":{"passes":["06:53","07:00","07:08"],"passesByDirection":{}}}"""),
                )

            val rows = repository.departures.getStopLineStarts("41", now = now)

            assertEquals(1, rows.size, "the line still calls here: $rows")
            assertEquals("4", rows.single().timetableId)

            assertEquals(
                Instant.parse("2026-10-16T04:53:00Z"),
                rows.single().departTimestamp!!,
            )
            assertEquals("PALACIO DE CONGRESOS", rows.single().displayName)
            assertNull(rows.single().delaySeconds, "a tomorrow bus is not a tracked bus")
        }

    @Test
    fun `given the day's first bus gone when rows are built then the next one is offered`() =
        runTest {
            val repository =
                repository(
                    overrides =
                        mapOf(
                            "passesByLine" to """{"result":{"passes":["00:00","01:00"],"passesByDirection":{"Ida":["00:00","01:00"]}}}""",
                        ),
                )

            val rows =
                repository.departures.getStopLineStarts(
                    "41",
                    now = Instant.parse("2026-10-14T22:45:00Z"),
                )

            assertEquals(1, rows.size, "the line still has a bus tonight: $rows")
            assertEquals(
                Instant.parse("2026-10-14T23:00:00Z"),
                rows.single().departTimestamp!!,
                "01:00 local, not tomorrow's 00:00",
            )
        }

    @Test
    fun `given a night line with no bus out when rows are built then it is not offered`() =
        runTest {
            val repository =
                repository(
                    overrides =
                        mapOf(
                            "passes" to """{"result":{"passes":[
                    {"lineRef":"31","stopPointRef":"157","journeyCode":"1","order":1,
                     "aimedArrivalTime":"2026-10-15T13:20:00+02:00","aimedDepartureTime":"2026-10-15T13:20:00+02:00"}
                ]}}""",
                        ),
                )

            val rows = repository.departures.getStopLineStarts("157", now = now)

            assertTrue(
                rows.isEmpty(),
                "no búho is out at midday, so its timetable is not a promise: $rows",
            )
        }

    @Test
    fun `given a stop no line calls at when rows are built then nothing is listed`() =
        runTest {
            val rows = repository().departures.getStopLineStarts("999", now = now)

            assertTrue(rows.isEmpty(), "stop 999 is on no sequence and in no catalogue: $rows")
        }
}
