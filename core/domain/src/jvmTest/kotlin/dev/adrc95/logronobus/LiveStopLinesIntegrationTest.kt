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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LiveStopLinesIntegrationTest {
    @Test
    fun `given the stops seen missing lines when they are read then every line that can come is listed`() =
        runTest {
            org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")

            val repository = TestBus()

            assertEquals(
                setOf("4", "10"),
                repository.departures
                    .getStopLineStarts("157")
                    .map { it.timetableId }
                    .toSet(),
                "every line that can come at Vitoria 157",
            )
            assertEquals(
                setOf("4", "10"),
                repository.departures
                    .getStopLineStarts("88")
                    .map { it.timetableId }
                    .toSet(),
                "every line that can come at Vitoria 88",
            )

            for (id in listOf("383", "384", "385", "40", "511", "676", "814")) {
                val rows = repository.departures.getStopLineStarts(id)
                assertEquals(
                    true,
                    rows.isNotEmpty(),
                    "stop $id lists the lines that call there: $rows",
                )
                println("LOGRONOBUS $id -> ${rows.joinToString { "${it.timetableId}·${it.displayName}@${it.departTimestamp}" }}")
            }
        }
}
