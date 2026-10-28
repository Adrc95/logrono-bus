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

import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.geo.indexOfBus
import dev.adrc95.logronobus.core.domain.trip.directionOf
import dev.adrc95.logronobus.core.domain.trip.stopEtasFor
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.testing.TestBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assume
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class LiveNextStopProbeIntegrationTest {
    @Test
    fun `given a night line's stop tonight when the app reads it then it shows a next stop`() =
        runTest {
            Assume.assumeTrue(System.getenv("LIVE_API") == "1")
            val repository = TestBus()
            for (stopRef in listOf("5", "202", "38")) {
                val departures = repository.departures.getStopDeparturesWithLive(stopRef)
                println("live: stop $stopRef -> ${departures.times.size} rows")
                departures.times.forEach { row ->
                    println(
                        "live:    line=${row.timetableId} at=${row.departTimestamp} " +
                            "vehicle=${row.vehicleId} status=${row.arrivalStatus} starts=${row.startsHere}",
                    )
                }
            }
        }
}
