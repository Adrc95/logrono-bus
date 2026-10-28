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

class NightLineNumberIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val now = Instant.parse("2026-10-16T23:50:00Z")

    private val stopCatalogue = """{"result":{"stops":[
        {"id":801,"name":"La Cava","lat":42.46,"lng":-2.44,"status":1,"lines":[31,9]},
        {"id":802,"name":"Varea","lat":42.461,"lng":-2.441,"status":1,"lines":[31,9]},
        {"id":803,"name":"La Estrella","lat":42.462,"lng":-2.442,"status":1,"lines":[31,9]}]}}"""

    private fun lineStop(
        id: Int,
        name: String,
    ) = """{"id":$id,"name":"$name","lat":42.46,"lng":-2.44,"status":1,"onwardShape":[]}"""

    private val lines = """{"result":{"lines":[
        {"id":31,"name":"B1-VAREA-LA CAVA","color":"rgba(253,2,0,1)","status":1,
         "stops":{"asc":[${lineStop(802, "Varea")},${lineStop(801, "La Cava")},${
        lineStop(
            803,
            "La Estrella",
        )
    }],
                  "desc":[${lineStop(803, "La Estrella")},${
        lineStop(
            801,
            "La Cava",
        )
    },${lineStop(802, "Varea")}]}},
        {"id":32,"name":"B2-EL ARCO-LA ESTRELLA","color":"rgba(0,2,253,1)","status":1,
         "stops":{"asc":[${lineStop(802, "Varea")},${lineStop(803, "La Estrella")}],
                  "desc":[${lineStop(803, "La Estrella")},${lineStop(802, "Varea")}]}},
        {"id":33,"name":"B3-LARDERO-EL CAMPILLO","color":"rgba(1,255,1,1)","status":1,
         "stops":{"asc":[${lineStop(802, "Varea")},${lineStop(803, "La Estrella")}],
                  "desc":[${lineStop(803, "La Estrella")},${lineStop(802, "Varea")}]}},
        {"id":9,"name":"9-PRADOVIEJO-LAS NORIAS","color":"rgba(0,2,253,1)","status":1,
         "stops":{"asc":[${lineStop(801, "La Cava")},${lineStop(802, "Varea")},${
        lineStop(
            803,
            "La Estrella",
        )
    }],
                  "desc":[${lineStop(803, "La Estrella")},${lineStop(802, "Varea")},${
        lineStop(
            801,
            "La Cava",
        )
    }]}}]}}"""

    private fun arrival(
        lineRef: String,
        order: Int,
    ) = """
        {"lineRef":"$lineRef","directionRef":"403","journeyCode":"","vehicleRef":"89",
         "stopPointRef":"801","order":$order,
         "aimedArrivalTime":"2026-10-17T02:00:00+02:00","expectedArrivalTime":"2026-10-17T02:00:59+02:00",
         "aimedDepartureTime":"2026-10-17T02:00:00+02:00","expectedDepartureTime":"2026-10-17T02:00:59+02:00",
         "arrivalStatus":"NO_REPORT","departureStatus":"NO_REPORT","cancellation":false,
         "predictionInaccurate":false,"delaySeconds":0}"""

    private val fleet = """{"result":{"activities":[
        {"vehicleRef":"89","lineRef":"31","directionRef":"Ida","publishedLineName":"VAREA-LA CAVA",
         "latitude":42.4605,"longitude":-2.4405,"locationRecordedAtTime":"2026-10-17T00:49:50Z",
         "delaySeconds":60,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0802","nextStopExpectedArrivalTime":"2026-10-17T00:52:00Z",
         "nextStopAimedArrivalTime":"2026-10-17T00:52:00Z"}]}}"""

    private fun repository(arrivalsBody: String) =
        TestBus(
            HttpClient(
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val body =
                        when {
                            path.endsWith("/api/linesDiscovery/stops") -> stopCatalogue
                            path.endsWith("/api/linesDiscovery/lines") -> lines
                            path.endsWith("/api/estimatedTimetable/byStop/801") -> arrivalsBody
                            path.endsWith("/api/productionTimetable/byStop/801") -> """{"result":{"passes":[]}}"""
                            path.endsWith("/api/vehicleMonitoring/all") -> fleet
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
    fun `given a ref the catalogue does not know when the row is placed then its order in the trip answers`() =
        runTest {
            val rows =
                repository("""{"result":{"arrivals":[${arrival("12", order = 1)}]}}""")
                    .departures
                    .getStopDeparturesWithLive("801", now = now)
                    .times

            assertEquals("9", rows.single().timetableId, "the row's order only fits line 9")
        }

    @Test
    fun `given a ref with nothing to place it by when it is mapped then it is kept as it came`() =
        runTest {
            val rows =
                repository("""{"result":{"arrivals":[${arrival("13", order = 9)}]}}""")
                    .departures
                    .getStopDeparturesWithLive("801", now = now)
                    .times

            assertEquals("13", rows.single().timetableId)
        }

    @Test
    fun `given a night line when its buses reach the map then they carry the public name and its colour`() =
        runTest {
            val feed = repository("""{"result":{"arrivals":[]}}""").vehicles.getBusPositions(now = now)

            val night = feed.byRoute["B1"].orEmpty()
            assertEquals(
                1,
                night.size,
                "the night bus is listed under the line the rider knows: ${feed.byRoute.keys}",
            )
            assertEquals("89", night.single().vehicleId)
            assertEquals("rgba(253,2,0,1)", night.single().lineColor, "B1's own colour, for the map")
        }
}
