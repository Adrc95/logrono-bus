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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class StaleFeedIntegrationTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val now = Instant.parse("2026-10-15T11:40:00Z")

    private fun repository(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = TestBus(
        HttpClient(
            MockEngine { request ->
                val answer = if (request.url.encodedPath.contains("/linesDiscovery/")) EMPTY_FEEDS else body
                respond(answer, status, jsonHeaders)
            },
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            expectSuccess = true
        },
    )

    private val oneFreshBus =
        """
        {"result":{"activities":[
          {"vehicleRef":"9693","lineRef":"1","directionRef":"Vuelta",
           "publishedLineName":"LARDERO-HOSPITAL SAN PEDRO","latitude":42.460321,
           "longitude":-2.445901,"locationRecordedAtTime":"2026-10-15T11:39:00Z",
           "delaySeconds":42,"monitored":true,"predictionInaccurate":false,
           "confidenceLevel":"CERTAIN","nextStopRef":"0036",
           "nextStopExpectedArrivalTime":"2026-10-15T13:41:00Z",
           "nextStopAimedArrivalTime":"2026-10-15T13:40:00Z"}]}}
        """.trimIndent()

    @Test
    fun `given a fresh fleet response when it is read then it is live`() =
        runTest {
            val feed = repository(oneFreshBus).vehicles.getBusPositions(now = now)
            assertEquals(1, feed.all.size)
            assertFalse(feed.stale)
            assertEquals("1", feed.forRoute("1").single().timetableId)
        }

    @Test
    fun `given an empty fleet response when it is read then it is night mode rather than an outage`() =
        runTest {
            val feed =
                repository("""{"result":{"activities":[]}}""").vehicles.getBusPositions(now = now)
            assertTrue(feed.all.isEmpty())
            assertFalse(feed.stale, "no buses running is not the same as no feed")
        }

    @Test
    fun `given an unreachable operator when the feed is read then it is stale`() =
        runTest {
            val feed =
                repository("", HttpStatusCode.ServiceUnavailable).vehicles.getBusPositions(now = now)
            assertTrue(feed.stale, "the buses are probably out there; we just cannot see them")
            assertTrue(feed.all.isEmpty())
        }

    @Test
    fun `given old records when the feed is read then they do not surface as buses`() =
        runTest {
            val old =
                """
                {"result":{"activities":[
                  {"vehicleRef":"1","lineRef":"2","directionRef":"Ida","publishedLineName":"YAGÜE-VAREA",
                   "latitude":42.46,"longitude":-2.44,"locationRecordedAtTime":"2026-10-15T09:00:00Z",
                   "delaySeconds":0,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
                   "nextStopRef":"0036","nextStopExpectedArrivalTime":"2026-10-15T11:40:00Z",
                   "nextStopAimedArrivalTime":"2026-10-15T11:40:00Z"}]}}
                """.trimIndent()
            val feed = repository(old).vehicles.getBusPositions(now = now)
            assertTrue(feed.all.isEmpty(), "a four-hour-old position is not a bus the rider can see")
            assertFalse(feed.stale, "the feed answered: there is simply nothing fresh in it")
        }

    @Test
    fun `given the whole feed when one line is asked then forRoute picks it`() =
        runTest {
            val feed = repository(oneFreshBus).vehicles.getBusPositions("1", now = now)
            assertEquals(1, feed.forRoute("1").size)
            assertTrue(feed.forRoute("6").isEmpty())
        }
}
