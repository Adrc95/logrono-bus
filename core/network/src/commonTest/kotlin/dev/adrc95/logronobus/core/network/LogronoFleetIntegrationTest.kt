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

package dev.adrc95.logronobus.core.network

import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.common.platform.ServerClock
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.network.NetworkConstants.NO_LINE
import dev.adrc95.logronobus.core.network.datasource.LogronoFleetDataSource
import dev.adrc95.logronobus.core.network.dto.ActivityDto
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueDataSource
import dev.adrc95.logronobus.core.testing.fake.FakeFleetService
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class LogronoFleetIntegrationTest {
    private val now = Instant.parse("2026-10-15T11:30:00Z")

    private val codes = mapOf(1 to "1", 2 to "2", 6 to "6")

    @BeforeTest
    fun noServerClock() {
        ServerClock.lastAnswerHadClock = false
        ServerClock.offsetMs = 0L
    }

    private fun activity(
        ref: String = "9693",
        line: String = "1",
        lat: Double = 42.46,
        lng: Double = -2.44,
        recordedAt: String = "2026-10-15T11:29:00Z",
        nextStop: String = "0036",
        expected: String = "2026-10-15T13:31:00Z",
        aimed: String = "2026-10-15T13:29:30Z",
        delay: Int? = 90,
        headsign: String = "LARDERO-HOSPITAL SAN PEDRO",
        direction: String = "Vuelta",
    ) = ActivityDto(
        vehicleRef = ref,
        lineRef = line,
        directionRef = direction,
        publishedLineName = headsign,
        latitude = lat,
        longitude = lng,
        locationRecordedAtTime = recordedAt,
        delaySeconds = delay,
        monitored = true,
        predictionInaccurate = false,
        confidenceLevel = "CERTAIN",
        nextStopRef = nextStop,
        nextStopExpectedArrivalTime = expected,
        nextStopAimedArrivalTime = aimed,
    )

    private suspend fun positions(vararg activities: ActivityDto): BusPositions {
        val datasource = LogronoFleetDataSource(FakeFleetService(activities.toList()), FakeCatalogueDataSource(codes = codes))
        val answer = datasource.positions(now, forceRefresh = true)
        return (answer as Result.Success).data
    }

    private suspend fun positionsWithStops(
        stopsByLine: Map<String, List<List<Stop>>>,
        vararg activities: ActivityDto,
    ): BusPositions {
        val datasource =
            LogronoFleetDataSource(
                FakeFleetService(activities.toList()),
                FakeCatalogueDataSource(codes = codes, stopsByLine = stopsByLine),
            )
        val answer = datasource.positions(now, forceRefresh = true)
        return (answer as Result.Success).data
    }

    @Test
    fun `given a fresh activity when it is mapped then it becomes a bus on its visible line code`() =
        runTest {
            val bus = positions(activity()).all.single()
            assertEquals("9693", bus.vehicleId)
            assertEquals(
                "9693",
                bus.tripDuid,
                "Logroño's vehicle ref is the only trip identity there is",
            )
            assertEquals("1", bus.timetableId, "lineRef already carries the visible code")
            assertEquals("LARDERO-HOSPITAL SAN PEDRO", bus.headsign)
            assertEquals(
                "36",
                bus.nextStopRef,
                "trap 7: nextStopRef is zero-padded, catalogue ids are not",
            )
            assertEquals(42.46, bus.latitude, 1e-9)
        }

    @Test
    fun `given a feed direction when it is mapped then it becomes the direction index`() =
        runTest {
            assertEquals(0, positions(activity(direction = "Ida")).all.single().directionIndex)
            assertEquals(1, positions(activity(direction = "Vuelta")).all.single().directionIndex)
            assertEquals(
                null,
                positions(activity(direction = "363")).all.single().directionIndex,
                "numeric refs come from other feeds and say nothing about the sense",
            )
        }

    @Test
    fun `given a next-stop prediction when it is mapped then it is carried as the only ahead stop`() =
        runTest {
            val bus = positions(activity()).all.single()
            val ahead = bus.nextStops!!.single()
            assertEquals("36", ahead.stopRef)

            assertEquals(Instant.parse("2026-10-15T11:31:00Z"), ahead.departureTimestamp)
            assertEquals(90, ahead.delay)
        }

    @Test
    fun `given junk rows when the feed is mapped then they are dropped`() =
        runTest {
            assertTrue(
                positions(activity(lat = 0.0, lng = 0.0, recordedAt = "2026-02-23T10:00:00Z")).all.isEmpty(),
                "lat/lng 0.0 rows are junk (trap 11)",
            )
            assertTrue(
                positions(activity(line = NO_LINE)).all.isEmpty(),
                "night-mode rows come back as [object Object] (trap 4)",
            )
            assertTrue(positions(activity(line = "")).all.isEmpty())
        }

    @Test
    fun `given a record older than the window when the feed is mapped then it is dropped`() =
        runTest {
            assertTrue(
                positions(activity(recordedAt = "2026-10-15T11:00:00Z")).all.isEmpty(),
                "a 30-minute-old position is not a bus the rider can see",
            )
            assertEquals(1, positions(activity(recordedAt = "2026-10-15T11:26:00Z")).all.size)
        }

    @Test
    fun `given no next-stop stamps when it is mapped then the ahead list is empty rather than wrong`() =
        runTest {
            val bus = positions(activity(expected = "", aimed = "", nextStop = "")).all.single()
            assertNull(bus.nextStopRef)
            assertTrue(bus.nextStops.isNullOrEmpty())
        }

    @Test
    fun `given a feed with nothing fresh when it is read then it is night mode rather than stale`() =
        runTest {
            val positions = positions(activity(recordedAt = "2026-10-15T02:00:00Z"))
            assertTrue(positions.all.isEmpty())
            assertTrue(!positions.stale, "night mode must not read as an outage")
        }

    @Test
    fun `given several buses when the live feed is read then they are grouped by line code`() =
        runTest {
            val positions =
                positions(
                    activity(ref = "A", line = "2"),
                    activity(ref = "B", line = "2"),
                    activity(ref = "C", line = "6"),
                )
            assertEquals(2, positions.forRoute("2").size)
            assertEquals(1, positions.forRoute("6").size)
            assertEquals(3, positions.all.size)
        }

    @Test
    fun `given a bus without a reported next stop when its position is known then the next stop is guessed`() =
        runTest {
            val stops =
                listOf(
                    stopAt("10", 42.4600, -2.4400),
                    stopAt("11", 42.4610, -2.4420),
                    stopAt("12", 42.4620, -2.4440),
                )
            val bus =
                positionsWithStops(
                    mapOf("1" to listOf(stops)),
                    activity(nextStop = "", expected = "", aimed = "", lat = 42.4615, lng = -2.4430),
                ).all.single()

            assertEquals("12", bus.nextStopRef)
            assertEquals(listOf("12"), bus.nextStops.orEmpty().map { it.stopRef })
        }

    @Test
    fun `given a bus with a reported next stop when its position is known then the report wins`() =
        runTest {
            val stops =
                listOf(
                    stopAt("10", 42.4600, -2.4400),
                    stopAt("11", 42.4610, -2.4420),
                    stopAt("12", 42.4620, -2.4440),
                )
            val bus =
                positionsWithStops(
                    mapOf("1" to listOf(stops)),
                    activity(nextStop = "0036", lat = 42.4615, lng = -2.4430),
                ).all.single()

            assertEquals("36", bus.nextStopRef)
        }

    @Test
    fun `given a good line feed when the next refresh fails then the last buses stay on the map`() =
        runTest {
            val service = FakeFleetService(listOf(activity()))
            val datasource = LogronoFleetDataSource(service, FakeCatalogueDataSource(codes = codes))
            datasource.positionsFor("1", now, forceRefresh = true)

            service.failing = true
            val answer = datasource.positionsFor("1", now + 31.seconds, forceRefresh = true)

            val positions = (answer as Result.Success).data
            assertEquals(1, positions.all.size)
            assertTrue(positions.stale)
        }

    @Test
    fun `given the last good buses are minutes old when the refresh fails then nothing is shown`() =
        runTest {
            val service = FakeFleetService(listOf(activity()))
            val datasource = LogronoFleetDataSource(service, FakeCatalogueDataSource(codes = codes))
            datasource.positionsFor("1", now, forceRefresh = true)

            service.failing = true
            val answer = datasource.positionsFor("1", now + 4.minutes, forceRefresh = true)

            assertTrue(answer is Result.Error)
        }

    private fun stopAt(
        ref: String,
        lat: Double,
        lng: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = "Stop $ref",
        shortName = "Stop $ref",
        latitude = lat,
        longitude = lng,
    )
}
