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

class Live202IntegrationTest {
    @Test
    fun `given manuel de falla when both directions are read then each answers`() =
        runTest {
            org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")
            val repo = TestBus()
            println("DIRS line 10: " + repo.catalogue.getDirectionHeadsigns("10"))
            println("DIRS line 32: " + repo.catalogue.getDirectionHeadsigns("32"))
            val dep = repo.departures.getStopDeparturesWithLive("202", lineFilter = "10").times
            println("stop 202 line 10 -> " + dep.map { "${it.timetableId}→${it.displayName} @${it.departTimestamp}" })
        }
}
