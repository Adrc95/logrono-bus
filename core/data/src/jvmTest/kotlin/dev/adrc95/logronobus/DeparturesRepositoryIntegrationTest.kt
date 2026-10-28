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
import dev.adrc95.logronobus.core.testing.stubbedBackend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeparturesRepositoryIntegrationTest {
    private val repository = TestBus(injectedHttpClient = stubbedBackend())

    @Test
    fun getStopDeparturesKeepsEveryDepartureTheWindowHolds() =
        runTest {
            val departures = repository.departures.getStopDeparturesWithLive("152").times
            assertTrue(
                departures.isNotEmpty(),
                "Expected the operator's departures to come through",
            )
            assertTrue(
                departures.size > 5,
                "Nothing is trimmed by count any more, so a busy stop keeps its sixth row: ${departures.size}",
            )
        }
}
