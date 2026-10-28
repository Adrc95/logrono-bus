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
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.builder.geoPoint
import dev.adrc95.logronobus.core.testing.builder.walkDestination
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LogronoBusRepositoryIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val now = Instant.parse("2026-10-15T11:25:00Z")

    private class Recorder {
        val urls = mutableListOf<String>()
        val bodies = mutableListOf<String>()
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "missing $name" }
            .bufferedReader()
            .readText()

    private fun fixtureBody(fixture: String): String = fixture(fixture)

    private fun router(path: String): String =
        when {
            path.endsWith("/api/linesDiscovery/stops") -> "fixtures/stops.json"
            path.endsWith("/api/linesDiscovery/lines") -> "fixtures/lines.json"
            path.contains("/api/estimatedTimetable/byStop/") -> "fixtures/estimatedTimetable.json"
            path.endsWith("/api/vehicleMonitoring/all") -> "fixtures/vehicleMonitoring.json"
            path.endsWith("/api/transit/walk-distances") -> "fixtures/walk-distances.json"
            else -> EMPTY_FEEDS
        }

    private fun load(fixture: String): String = fixtureBody(fixture)

    private fun repository(recorder: Recorder = Recorder()): TestBus {
        val client =
            HttpClient(
                MockEngine { request ->
                    recorder.urls.add(request.url.toString())
                    (request.body as? TextContent)?.let { recorder.bodies.add(it.text) }
                    respond(fixtureBody(router(request.url.encodedPath)), HttpStatusCode.OK, jsonHeaders)
                },
            ) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                install(HttpRequestRetry) {
                    maxRetries = 2
                    retryOnExceptionOrServerErrors(maxRetries)
                }
                expectSuccess = true
            }
        return TestBus(injectedHttpClient = client)
    }

    private fun decoded(request: String): String = java.net.URLDecoder.decode(request, "UTF-8")

    @Test
    fun `given the line catalogue when the routes are read then they come keyed by visible code`() =
        runTest {
            val routes = repository().catalogue.getRoutes()
            assertEquals(listOf("6", "B1"), routes.keys.sorted())
            assertEquals("6-CENTRO-EL CORTIJO", routes.getValue("6").longName)
            assertEquals("6", routes.getValue("6").shortName)
            assertEquals(
                "B1",
                routes.getValue("B1").shortName,
                "31 is the night line's id, B1 its code (trap 18)",
            )
            assertEquals("rgba(253,2,0,1)", routes.getValue("B1").color)
        }

    @Test
    fun `given the catalogue when the stops are read then they carry visible codes and are fetched once`() =
        runTest {
            val recorder = Recorder()
            val repo = repository(recorder)
            val stops = repo.catalogue.getStops()
            repo.catalogue.getStops()
            assertEquals(
                1,
                recorder.urls.count { it.endsWith("/api/linesDiscovery/stops") },
                "catalogues are cached",
            )
            val vitoria = stops.first { it.longName == "Vitoria" }
            assertEquals(listOf("B1", "10", "4"), vitoria.routes)
            assertEquals("157", vitoria.stopRef)
        }

    @Test
    fun `given a stop when its arrivals are asked then line ids are sent and the window stays on the client`() =
        runTest {
            val recorder = Recorder()
            val departures =
                repository(recorder)
                    .departures
                    .getStopDeparturesWithLive("152", now = now, windowMinutes = 90)
                    .times
            val request = recorder.urls.single { it.contains("estimatedTimetable") }
            val decoded = decoded(request)
            assertTrue(
                decoded.contains("lines=6"),
                "trap 1: lines is mandatory, with the stop's internal ids: $decoded",
            )
            assertTrue(
                decoded.contains("previewMinutes=90"),
                "asked wide, filtered here",
            )
            assertEquals(
                3,
                departures.size,
                "the 23:50 row is outside the window (trap 13)",
            )
            assertEquals(
                listOf(
                    Instant.parse("2026-10-15T13:45:30+02:00"),
                    Instant.parse("2026-10-15T14:00:00+02:00"),
                    Instant.parse("2026-10-15T14:19:00+02:00"),
                ),
                departures.mapNotNull { it.departTimestamp }.sorted(),
            )
        }

    @Test
    fun `given an hour with no live service when the next departure is built then it has the same shape as any other`() =
        runTest {
            val night = Instant.parse("2026-10-15T22:35:00Z")
            val recorder = Recorder()
            val departures =
                repository(recorder)
                    .departures
                    .getStopDeparturesWithLive(
                        "152",
                        now = night,
                        windowMinutes = 90,
                    ).times

            val requests = recorder.urls.filter { it.contains("estimatedTimetable") }
            assertEquals(
                2,
                requests.size,
                "a narrow ask, then a wide one",
            )
            assertTrue(
                requests.last().contains("previewMinutes=1440"),
                "the wide one: ${requests.last()}",
            )

            assertEquals(
                1,
                departures.size,
                "the trip the operator actually has, not a note about it",
            )
            val first = departures.single()
            assertEquals(
                "6",
                first.timetableId,
                "its own line badge",
            )
            assertEquals(
                Instant.parse("2026-10-16T07:00:00+02:00"),
                first.departTimestamp,
                "its own clock time",
            )
            assertEquals(0, first.delaySeconds)
        }

    @Test
    fun `given a stop outside the catalogue when its departures are asked then the network is not called`() =
        runTest {
            val recorder = Recorder()
            val departures =
                repository(recorder)
                    .departures
                    .getStopDeparturesWithLive(
                        "999999",
                        now = now,
                    ).times
            assertTrue(departures.isEmpty())
            assertTrue(
                recorder.urls.none { it.contains("estimatedTimetable") },
                "without its lines the API answers an empty list anyway — don't spend the call",
            )
        }

    @Test
    fun `given a line filter when the departures are asked then the internal ids are sent`() =
        runTest {
            val recorder = Recorder()
            repository(recorder).departures.getStopDeparturesWithLive("61", now = now)
            val decoded =
                decoded(
                    recorder.urls.single {
                        it.contains("estimatedTimetable")
                    },
                )

            assertTrue(
                decoded.contains("lines=31,32,6,2,4,7,3,10"),
                "internal ids, catalogue order: $decoded",
            )
        }

    @Test
    fun `given a fleet payload with junk when the positions are read then they are grouped by visible code and the junk dropped`() =
        runTest {
            val feed = repository().vehicles.getBusPositions(now = now)
            val line6 = feed.forRoute("6")
            assertTrue(line6.isNotEmpty(), "fixture has fresh line 6 vehicles")
            assertTrue(
                feed.all.none { it.vehicleId == "0" || it.vehicleId == "0002" },
                "junk rows are dropped",
            )
            assertTrue(line6.all { it.timetableId == "6" })
            assertTrue(!feed.stale, "a 200 with data is not an outage")
        }

    @Test
    fun `given a route when its detail is read then the stops headsigns and shapes come from the catalogue`() =
        runTest {
            val repo = repository()
            val stops = repo.catalogue.getStopsForRoute("6")
            assertEquals(2, stops.size, "asc and desc")
            assertEquals(
                "152",
                stops[1].first().stopRef,
                "desc starts at Plaza de la iglesia El Cortijo",
            )
            val headsigns = repo.catalogue.getDirectionHeadsigns("6")
            assertEquals(
                "Blanco Lac",
                headsigns[1],
                "the direction's last stop is its destination",
            )
            val shapes = repo.catalogue.getRouteShapes("6")
            assertEquals(8, shapes[1].size, "4 stops x 2 shape points from onwardShape")
        }

    @Test
    fun `given nearby stops when their walking times are asked then they come from the walking service`() =
        runTest {
            val recorder = Recorder()
            val walked =
                repository(recorder).walking.walkDistances(
                    origin =
                        geoPoint {
                            withLat(42.4653)
                            withLon(-2.4448)
                        },
                    destinations =
                        listOf(
                            walkDestination {
                                withId("611")
                                withLat(42.457834)
                                withLng(-2.441812)
                            },
                        ),
                )
            assertEquals(1347, walked.single().distanceM)
            assertEquals(17, walked.single().durationMin)
            assertTrue(recorder.urls.single().contains("/api/transit/walk-distances"))
            assertTrue(
                recorder.bodies.single().contains("\"origin\""),
                "the service takes a JSON body, not query params",
            )
        }

    @Test
    fun `given favourites when they are saved and read back then they are the same`() =
        runTest {
            val repo = repository()

            repo.favourites.replaceFavouriteStops(emptyList())
            repo.lastViewedRoute.saveLastViewedRoute(null)
            repo.favourites.replaceFavouriteStops(
                listOf(
                    favouriteStop {
                        withStopRef("152")
                        withName("Plaza de la iglesia El Cortijo")
                        withStopId("152")
                    },
                ),
            )
            assertEquals(
                1,
                repo.favourites
                    .observeFavouriteStops()
                    .first()
                    .size,
            )
            assertNull(repo.lastViewedRoute.getLastViewedRoute())
            repo.lastViewedRoute.saveLastViewedRoute("6")
            assertEquals("6", repo.lastViewedRoute.getLastViewedRoute())
            repo.favourites.replaceFavouriteStops(emptyList())
            repo.lastViewedRoute.saveLastViewedRoute(null)
        }

    @Test
    fun `given a learned direction when it is saved and read back then it is the same`() =
        runTest {
            val repo = repository()
            repo.departures.rememberDirection("6", "363", "El Cortijo")

            assertEquals("El Cortijo", repository().departures.learnedDirections()["6"]?.get("363"))
        }

    @Test
    fun `given a transient failure when the call is made then it is retried before giving up`() =
        runTest {
            var calls = 0
            val client =
                HttpClient(
                    MockEngine { request ->
                        calls++
                        if (calls == 1) {
                            respondError(HttpStatusCode.ServiceUnavailable)
                        } else {
                            respond(load(router(request.url.encodedPath)), HttpStatusCode.OK, jsonHeaders)
                        }
                    },
                ) {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                    install(HttpRequestRetry) {
                        maxRetries = 2
                        retryOnExceptionOrServerErrors(
                            maxRetries,
                        )
                    }
                    expectSuccess = true
                }
            val repo = TestBus(injectedHttpClient = client)
            assertEquals(254, repo.catalogue.getStops().size)
            assertTrue(
                calls >= 2,
                "the API scales to zero: one retry ladder absorbs the cold start",
            )
        }
}
