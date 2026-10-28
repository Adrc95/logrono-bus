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

import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.testing.TestBus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
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

class DepartureVehicleIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val stopId = "152"

    private val stopCatalogue =
        """{"result":{"stops":[{"id":152,"name":"Plaza de la iglesia El Cortijo","lat":42.492503,"lng":-2.520566,"status":1,"lines":[6]}]}}"""

    private fun arrival(
        line: String = "6",
        minutesAhead: Long,
        vehicle: String = "",
        journey: String = "",
        cancelled: Boolean = false,
        order: Int = 11,
    ): String {
        val at =
            java.time.Instant
                .now()
                .plusSeconds(minutesAhead * 60)
        val local =
            at
                .atZone(java.time.ZoneId.of("Europe/Madrid"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return """
            {"lineRef":"$line","directionRef":"","journeyCode":"$journey","vehicleRef":"$vehicle",
             "stopPointRef":"152","order":$order,"aimedArrivalTime":"$local","expectedArrivalTime":"$local",
             "aimedDepartureTime":"$local","expectedDepartureTime":"$local","arrivalStatus":"scheduled",
             "departureStatus":"scheduled","cancellation":$cancelled,"predictionInaccurate":false,"delaySeconds":0}
            """.trimIndent()
    }

    private fun repository(arrivals: String?) =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                            path.endsWith("/api/linesDiscovery/lines") -> """{"result":{"lines":[]}}"""
                            path.contains("/api/estimatedTimetable/byStop/") -> arrivals
                            else -> null
                        }
                    if (body == null) {
                        respondError(HttpStatusCode.InternalServerError)
                    } else {
                        respond(body, HttpStatusCode.OK, jsonHeaders)
                    }
                },
            ) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                expectSuccess = true
            },
        )

    @Test
    fun `given an arrival with the feed's own number when it is mapped then it is kept and not looked up on the map`() =
        runTest {
            val times =
                repository(
                    """{"result":{"arrivals":[${
                        arrival(
                            minutesAhead = 4,
                            vehicle = "9693",
                            journey = "14442",
                        )
                    }]}}""",
                ).departures
                    .getStopDeparturesWithLive(stopId)
                    .times
            assertEquals(
                "9693",
                times.single().vehicleId,
                "the arrivals feed's numbering, as the feed gave it",
            )
            assertEquals("9693", times.single().tripId)
        }

    @Test
    fun `given a departure with no vehicle when it is mapped then it is shown but not trackable`() =
        runTest {
            val times =
                repository("""{"result":{"arrivals":[${arrival(minutesAhead = 12)}]}}""")
                    .departures
                    .getStopDeparturesWithLive(stopId)
                    .times
            assertNull(times.single().vehicleId, "no vehicle: nothing to follow, and nothing to invent")
            assertNull(times.single().tripId)
            assertTrue(
                times.single().displayName.isEmpty(),
                "no sequence matched either: no destination claimed",
            )
        }

    @Test
    fun `given a cancelled call when it is mapped then it never appears as due`() =
        runTest {
            val times =
                repository(
                    """{"result":{"arrivals":[${
                        arrival(
                            minutesAhead = 3,
                            cancelled = true,
                        )
                    }]}}""",
                ).departures
                    .getStopDeparturesWithLive(stopId)
                    .times
            assertTrue(times.isEmpty())
        }

    @Test
    fun `given a failed fetch when the departures are read then they are not live nor an empty stop`() {
        var departures: StopDepartures? = null
        runTest { departures = repository(arrivals = null).departures.getStopDeparturesWithLive(stopId) }
        val answer = departures ?: error("no departures")
        assertTrue(answer.times.isEmpty(), "no rows are invented out of a failure")
        assertTrue(answer.stale, "the operator could not be reached: that is not an empty stop")
    }

    @Test
    fun `given several departures when they are mapped then each keeps its own vehicle`() =
        runTest {
            val times =
                repository(
                    """{"result":{"arrivals":[
                ${arrival(minutesAhead = 4, vehicle = "9693")},
                ${arrival(minutesAhead = 15, vehicle = "7340")}]}}""",
                ).departures.getStopDeparturesWithLive(stopId).times
            assertEquals(listOf("9693", "7340"), times.map { it.vehicleId })
        }
}
