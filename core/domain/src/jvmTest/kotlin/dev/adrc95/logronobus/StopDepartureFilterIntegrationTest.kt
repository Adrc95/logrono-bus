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
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class StopDepartureFilterIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val now = Instant.parse("2026-10-15T11:25:00Z")

    private val stopCatalogue =
        """{"result":{"stops":[{"id":58,"name":"Palacio de Congresos","lat":42.46,"lng":-2.44,"status":1,"lines":[1,4,31]}]}}"""

    private val arrivals = """{"result":{"arrivals":[
        {"lineRef":"4","directionRef":"d1","journeyCode":"J1","vehicleRef":"70","stopPointRef":"58","order":9,
         "aimedArrivalTime":"2026-10-15T13:30:00+02:00","expectedArrivalTime":"2026-10-15T13:29:00+02:00",
         "aimedDepartureTime":"2026-10-15T13:30:00+02:00","expectedDepartureTime":"2026-10-15T13:29:00+02:00",
         "arrivalStatus":"ARRIVING","departureStatus":"ONTIME","cancellation":false,"predictionInaccurate":false,"delaySeconds":-60},
        {"lineRef":"1","directionRef":"d2","journeyCode":"J2","vehicleRef":"59","stopPointRef":"58","order":3,
         "aimedArrivalTime":"2026-10-15T13:34:00+02:00","expectedArrivalTime":"2026-10-15T13:37:00+02:00",
         "aimedDepartureTime":"2026-10-15T13:34:00+02:00","expectedDepartureTime":"2026-10-15T13:37:00+02:00",
         "arrivalStatus":"SCHEDULED","departureStatus":"SCHEDULED","cancellation":false,"predictionInaccurate":false,"delaySeconds":180}
    ]}}"""

    private class Recorder {
        val urls = mutableListOf<String>()
        val bodies = mutableListOf<String>()
    }

    private fun repository(recorder: Recorder) =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    recorder.urls.add(request.url.toString())
                    (request.body as? TextContent)?.let { recorder.bodies.add(it.text) }
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                            path.endsWith("/api/linesDiscovery/lines") -> LINES
                            path.contains("/api/estimatedTimetable/byStop/") -> arrivals
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
    fun `given line 4 open at a shared stop when the rows are built then only line 4 is shown`() =
        runTest {
            val recorder = Recorder()
            val departures =
                repository(recorder)
                    .departures
                    .getStopDeparturesWithLive("58", now = now, lineFilter = "4")
                    .times
            assertEquals(listOf("4"), departures.map { it.timetableId }, "the rider asked about line 4")
            assertEquals(1, departures.size)
            val request = recorder.urls.single { it.contains("estimatedTimetable") }

            assertTrue(
                java.net.URLDecoder
                    .decode(request, "UTF-8")
                    .contains("lines=4"),
                "asked for the line in question only: $request",
            )
        }

    @Test
    fun `given no line filter when the rows are built then every line serving the stop comes`() =
        runTest {
            val departures =
                repository(Recorder()).departures.getStopDeparturesWithLive("58", now = now).times
            assertEquals(listOf("4", "1"), departures.map { it.timetableId })
        }

    @Test
    fun `given a line that does not serve the stop when it is filtered then nothing is shown and nothing is asked`() =
        runTest {
            val recorder = Recorder()
            val departures =
                repository(recorder)
                    .departures
                    .getStopDeparturesWithLive("58", now = now, lineFilter = "9")
                    .times
            assertTrue(departures.isEmpty(), "line 9 does not stop here")
            assertTrue(
                recorder.urls.none { it.contains("estimatedTimetable") },
                "no point asking for a line the catalogue says never comes",
            )
        }

    private fun repositoryWithOneSidedStop() =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> ONE_SIDED_STOPS
                            path.endsWith("/api/linesDiscovery/lines") -> ONE_SIDED_LINES
                            path.contains("/api/estimatedTimetable/byStop/") -> """{"result":{"arrivals":[]}}"""
                            path.contains("/api/productionTimetable/byStop/") -> """{"result":{"passes":[]}}"""
                            path.contains("/api/productionTimetable/byLine/") -> ONE_SIDED_PASSES
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
    fun `given a direction that skips the stop when it is filtered then nothing is shown`() =
        runTest {
            val repository = repositoryWithOneSidedStop()

            val desc =
                repository.departures
                    .getStopDeparturesWithLive("76", now = now, lineFilter = "2", directionFilter = 1)
                    .times
            assertTrue(desc.isEmpty(), "line 2's desc does not call at Estanque: $desc")

            val asc =
                repository.departures
                    .getStopDeparturesWithLive("76", now = now, lineFilter = "2", directionFilter = 0)
                    .times
            assertEquals(1, asc.size, "the direction that does call there keeps its bus: $asc")
            assertEquals("2", asc.single().timetableId)
            assertEquals(0, asc.single().directionIndex)
        }

    private companion object {
        const val LINES = """{"result":{"lines":[
            {"id":4,"name":"4-PALACIO DE CONGRESOS-PRADOVIEJOO","color":"rgba(253,2,0,1)","status":1,
             "stops":{"asc":[{"id":837,"name":"Alzheimer","lat":42.4,"lng":-2.4,"status":1,"onwardShape":[]},
                            {"id":58,"name":"Palacio de Congresos","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]}],
                      "desc":[{"id":58,"name":"Palacio de Congresos","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]},
                              {"id":837,"name":"Alzheimer","lat":42.4,"lng":-2.4,"status":1,"onwardShape":[]}]}},
            {"id":1,"name":"1-LARDERO-HOSPITAL SAN PEDRO","color":"rgba(38,153,5,1)","status":1,
             "stops":{"asc":[{"id":58,"name":"Palacio de Congresos","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]}],
                      "desc":[{"id":58,"name":"Palacio de Congresos","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]}]}}]}}"""

        const val ONE_SIDED_STOPS = """{"result":{"stops":[
            {"id":76,"name":"Estanque","lat":42.4629,"lng":-2.4640,"status":1,"lines":[2]}]}}"""

        const val ONE_SIDED_LINES = """{"result":{"lines":[
            {"id":2,"name":"2-YAGÜE-VAREA","color":"rgba(253,2,0,1)","status":1,
             "stops":{"asc":[{"id":56,"name":"Yagüe","lat":42.47,"lng":-2.47,"status":1,"onwardShape":[]},
                            {"id":76,"name":"Estanque","lat":42.4629,"lng":-2.4640,"status":1,"onwardShape":[]},
                            {"id":5,"name":"Varea","lat":42.45,"lng":-2.42,"status":1,"onwardShape":[]}],
                      "desc":[{"id":5,"name":"Varea","lat":42.45,"lng":-2.42,"status":1,"onwardShape":[]},
                              {"id":42,"name":"La Cava","lat":42.44,"lng":-2.44,"status":1,"onwardShape":[]},
                              {"id":56,"name":"Yagüe","lat":42.47,"lng":-2.47,"status":1,"onwardShape":[]}]}}]}}"""

        const val ONE_SIDED_PASSES = """{"result":{"passes":["13:40","14:00"],
            "passesByDirection":{"Ida":["13:40","14:00"],"Vuelta":["13:45","14:05"]}}}"""
    }
}
