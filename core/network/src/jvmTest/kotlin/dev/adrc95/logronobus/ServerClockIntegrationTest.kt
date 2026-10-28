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

import dev.adrc95.logronobus.core.common.platform.ServerClock
import dev.adrc95.logronobus.core.common.platform.deviceNowEpochMilliseconds
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.network.installServerClock
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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import java.time.Instant as JavaInstant

class ServerClockIntegrationTest {
    @AfterTest
    fun putTheClockBack() {
        ServerClock.offsetMs = 0L
        ServerClock.lastAnswerHadClock = false
    }

    private fun jsonHeaders(date: String? = null) =
        if (date == null) {
            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        } else {
            headersOf(
                HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
                HttpHeaders.Date to listOf(date),
            )
        }

    private fun dateHeader(epochMs: Long): String =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            JavaInstant.ofEpochMilli(epochMs).atZone(ZoneOffset.UTC),
        )

    private fun fleetOut(recordedIso: String) =
        """{"result":{"activities":[
        {"vehicleRef":"8348","lineRef":"2","directionRef":"Vuelta","publishedLineName":"YAGÜE-VAREA",
         "latitude":42.462905,"longitude":-2.445732,"locationRecordedAtTime":"$recordedIso",
         "delaySeconds":0,"monitored":true,"predictionInaccurate":false,"confidenceLevel":"CERTAIN",
         "nextStopRef":"0005","nextStopExpectedArrivalTime":"2026-10-16T17:16:31Z",
         "nextStopAimedArrivalTime":"2026-10-16T17:15:54Z"}]}}"""

    private fun repository(
        serverMs: Long?,
        body: String,
    ) = TestBus(
        HttpClient(
            MockEngine { request ->
                val answer =
                    if (request.url.encodedPath.contains("/linesDiscovery/")) EMPTY_FEEDS else body
                respond(
                    answer,
                    HttpStatusCode.OK,
                    jsonHeaders(serverMs?.let(::dateHeader)),
                )
            },
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            installServerClock()
            expectSuccess = true
        },
    )

    @Test
    fun `given a phone clock running fast when the bus reports now then the bus is not stale`() =
        runTest {
            val deviceMs = deviceNowEpochMilliseconds()
            val serverMs = deviceMs - 10 * 60_000L
            val recorded = Instant.fromEpochMilliseconds(serverMs - 3_000)
            ServerClock.offsetMs = 0L

            val feed =
                repository(serverMs, fleetOut(recorded.toString()))
                    .vehicles
                    .getBusPositions(
                        now = Instant.fromEpochMilliseconds(deviceMs),
                        forceRefresh = true,
                    )

            assertEquals(
                1,
                feed.all.size,
                "the whole fleet disappeared behind a phone that is ten minutes fast",
            )
            assertEquals("8348", feed.all.single().vehicleId)
            assertFalse(feed.stale, "the operator answered: nothing is out of service here")
        }

    @Test
    fun `given no operator clock when the fleet is judged then the phone's own clock answers`() =
        runTest {
            val deviceMs = deviceNowEpochMilliseconds()
            val recorded = Instant.fromEpochMilliseconds(deviceMs - 10 * 60_000L - 3_000)
            ServerClock.offsetMs = 0L

            val feed =
                repository(null, fleetOut(recorded.toString()))
                    .vehicles
                    .getBusPositions(
                        now = Instant.fromEpochMilliseconds(deviceMs),
                        forceRefresh = true,
                    )

            assertTrue(feed.all.isEmpty())
            assertFalse(feed.stale)
        }

    @Test
    fun `given the operator's clock when it is read then it becomes the app's clock`() =
        runTest {
            val deviceMs = deviceNowEpochMilliseconds()
            val serverMs = deviceMs - 10 * 60_000L
            val recorded = Instant.fromEpochMilliseconds(serverMs)
            ServerClock.offsetMs = 0L

            repository(serverMs, fleetOut(recorded.toString()))
                .vehicles
                .getBusPositions(
                    now = Instant.fromEpochMilliseconds(deviceMs),
                    forceRefresh = true,
                )

            val corrected = nowEpochMilliseconds() - deviceNowEpochMilliseconds()
            assertTrue(
                corrected in -11 * 60_000..-9 * 60_000,
                "ten minutes fast, and the app now counts in the operator's terms: $corrected ms off",
            )
        }
}
