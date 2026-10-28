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

class OriginStopDeparturesIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val now = Instant.parse("2026-10-16T14:31:00Z")

    private val stopCatalogue = """{"result":{"stops":[
        {"id":58,"name":"Palacio de Congresos","lat":42.468303,"lng":-2.431076,"status":1,"lines":[4]},
        {"id":212,"name":"Palacio de Congresos","lat":42.468484,"lng":-2.430941,"status":1,"lines":[4]},
        {"id":837,"name":"Alzheimer","lat":42.4,"lng":-2.4,"status":1,"lines":[4]}]}}"""

    private val lines = """{"result":{"lines":[
        {"id":4,"name":"4-PALACIO DE CONGRESOS-PRADOVIEJOO","color":"rgba(253,2,0,1)","status":1,
         "stops":{"asc":[{"id":58,"name":"Palacio de Congresos","lat":42.468303,"lng":-2.431076,"status":1,"onwardShape":[]},
                         {"id":837,"name":"Alzheimer","lat":42.4,"lng":-2.4,"status":1,"onwardShape":[]}],
                  "desc":[{"id":837,"name":"Alzheimer","lat":42.4,"lng":-2.4,"status":1,"onwardShape":[]},
                          {"id":212,"name":"Palacio de Congresos","lat":42.468484,"lng":-2.430941,"status":1,"onwardShape":[]},
                          {"id":58,"name":"Palacio de Congresos","lat":42.468303,"lng":-2.431076,"status":1,"onwardShape":[]}]}}]}}"""

    private val arrivalsAt58 = """{"result":{"arrivals":[
        {"lineRef":"4","directionRef":"363","journeyCode":"16861","vehicleRef":"81","stopPointRef":"58","order":3,
         "aimedArrivalTime":"2026-10-16T16:45:35+02:00","expectedArrivalTime":"2026-10-16T16:50:35+02:00",
         "aimedDepartureTime":"","expectedDepartureTime":"",
         "arrivalStatus":"ARRIVING","departureStatus":"","cancellation":false,"predictionInaccurate":false,"delaySeconds":300}]}}"""

    private val arrivalsAt212 = """{"result":{"arrivals":[
        {"lineRef":"4","directionRef":"363","journeyCode":"16861","vehicleRef":"81","stopPointRef":"212","order":2,
         "aimedArrivalTime":"2026-10-16T16:45:35+02:00","expectedArrivalTime":"2026-10-16T16:50:35+02:00",
         "aimedDepartureTime":"2026-10-16T16:45:35+02:00","expectedDepartureTime":"2026-10-16T16:50:35+02:00",
         "arrivalStatus":"ARRIVING","departureStatus":"ONTIME","cancellation":false,"predictionInaccurate":false,"delaySeconds":300}]}}"""

    private val passesAt58 = """{"result":{"passes":[
        {"lineRef":"4","stopPointRef":"58","journeyCode":"16804","order":1,
         "aimedArrivalTime":"2026-10-16T16:45:00+02:00","aimedDepartureTime":"2026-10-16T16:45:00+02:00"},
        {"lineRef":"4","stopPointRef":"58","journeyCode":"16861","order":3,
         "aimedArrivalTime":"2026-10-16T16:45:35+02:00","aimedDepartureTime":"2026-10-16T16:45:35+02:00"}]}}"""

    private fun repository() =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                            path.endsWith("/api/linesDiscovery/lines") -> lines
                            path.endsWith("/api/estimatedTimetable/byStop/58") -> arrivalsAt58
                            path.endsWith("/api/estimatedTimetable/byStop/212") -> arrivalsAt212
                            path.contains("/api/productionTimetable/byStop/58") -> passesAt58
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
    fun `given an asc origin when its rows are read then the departure leaving it is shown`() =
        runTest {
            val departures =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "58",
                        now = now,
                        lineFilter = "4",
                        directionFilter = 0,
                    ).times

            assertEquals(1, departures.size, "one line, one departure: the one that belongs here")
            val row = departures.single()
            assertEquals("4", row.timetableId)
            assertEquals(0, row.directionIndex, "the asc direction — the one being browsed")
            assertEquals(Instant.parse("2026-10-16T16:45:00+02:00"), row.departTimestamp)
            assertNull(row.delaySeconds, "nobody is tracking it yet: the timetable, not a bus")
            assertTrue(!row.terminatesHere, "the trip is leaving, not ending")
        }

    @Test
    fun `given an asc direction when rows are built then the desc kerbside is not used`() =
        runTest {
            val asc =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "212",
                        now = now,
                        lineFilter = "4",
                        directionFilter = 0,
                    ).times
            assertTrue(asc.isEmpty(), "a desc bus is not an asc departure")

            val desc =
                repository()
                    .departures
                    .getStopDeparturesWithLive(
                        "212",
                        now = now,
                        lineFilter = "4",
                        directionFilter = 1,
                    ).times
            assertEquals(1, desc.size)
            assertEquals(1, desc.single().directionIndex, "the desc direction, the one that ends here")
        }
}
