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

import dev.adrc95.logronobus.core.testing.TestBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LivePanelDeparturesIntegrationTest {
    @Test
    fun `given line 2 at estanque when the feed's departure has gone then the next one is kept`() =
        runTest {
            org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")
            val repository = TestBus()

            val asc =
                repository.departures
                    .getStopDeparturesWithLive(
                        "76",
                        lineFilter = "2",
                        directionFilter = 0,
                    ).times
            assertTrue(asc.isNotEmpty(), "the asc direction has buses after the feed's own goes: $asc")
            assertEquals(setOf("2"), asc.map { it.timetableId }.toSet())
            assertTrue(
                asc.all { it.directionIndex == null || it.directionIndex == 0 },
                "asc rows only: $asc",
            )
            println("LOGRONOBUS panel 76 asc -> ${asc.joinToString { "${it.timetableId}·${it.displayName}@${it.departTimestamp} delay=${it.delaySeconds}" }}")

            val desc =
                repository.departures
                    .getStopDeparturesWithLive(
                        "76",
                        lineFilter = "2",
                        directionFilter = 1,
                    ).times
            assertTrue(desc.isEmpty(), "line 2's desc never calls at 76: $desc")
        }

    @Test
    fun `given line 4's asc origin when rows are built then a departure of its own direction is shown`() =
        runTest {
            org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")
            val repository = TestBus()

            val rows =
                repository.departures
                    .getStopDeparturesWithLive(
                        "58",
                        lineFilter = "4",
                        directionFilter = 0,
                    ).times
            assertTrue(rows.isNotEmpty(), "the asc origin has departures of its own: $rows")
            assertTrue(
                rows.all { it.directionIndex == null || it.directionIndex == 0 },
                "nothing of the other direction belongs at the asc origin: $rows",
            )
            println("LOGRONOBUS panel 58 asc -> ${rows.joinToString { "${it.timetableId}·${it.displayName}@${it.departTimestamp} dir=${it.directionIndex} delay=${it.delaySeconds}" }}")
        }
}
