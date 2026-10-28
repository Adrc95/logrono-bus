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

import dev.adrc95.logronobus.core.data.NetworkFirstStrategy
import dev.adrc95.logronobus.core.data.OfflineFirstStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheStrategyTest {
    @Test
    fun `given a fresh local copy when offline first reads then the local data is served`() =
        runTest {
            val strategy = OfflineFirstStrategy()
            var remoteCalled = false
            var saved = false

            val result =
                strategy.execute(
                    remote = {
                        remoteCalled = true
                        listOf("remote")
                    },
                    local = { listOf("local") },
                    save = { saved = true },
                    isExpired = { false },
                )

            assertEquals(listOf("local"), result)
            assertFalse(remoteCalled, "the network is not touched while the data is fresh")
            assertFalse(saved)
        }

    @Test
    fun `given an expired local copy when offline first reads then it refreshes from the network`() =
        runTest {
            val strategy = OfflineFirstStrategy()
            var saved: List<String>? = null

            val result =
                strategy.execute(
                    remote = { listOf("remote") },
                    local = { listOf("local") },
                    save = { saved = it },
                    isExpired = { true },
                )

            assertEquals(listOf("remote"), result)
            assertEquals(listOf("remote"), saved)
        }

    @Test
    fun `given the network fails when offline first reads then the stale local data is served`() =
        runTest {
            val strategy = OfflineFirstStrategy()

            val result =
                strategy.execute(
                    remote = { error("no network") },
                    local = { listOf("local") },
                    save = { },
                    isExpired = { true },
                )

            assertEquals(listOf("local"), result)
        }

    @Test
    fun `given an empty local answer when offline first reads then it goes to the network`() =
        runTest {
            val strategy = OfflineFirstStrategy()
            var remoteCalled = false

            val result =
                strategy.execute(
                    remote = {
                        remoteCalled = true
                        listOf("remote")
                    },
                    local = { emptyList() },
                    save = { },
                    isExpired = { false },
                )

            assertEquals(listOf("remote"), result)
            assertTrue(remoteCalled)
        }

    @Test
    fun `given network first when it reads then it asks the network and saves the answer`() =
        runTest {
            val strategy = NetworkFirstStrategy()
            var saved: List<String>? = null

            val result =
                strategy.execute(
                    remote = { listOf("remote") },
                    local = { listOf("local") },
                    save = { saved = it },
                    isExpired = { false },
                )

            assertEquals(listOf("remote"), result)
            assertEquals(listOf("remote"), saved)
        }

    @Test
    fun `given the network fails when network first reads then the local data is served`() =
        runTest {
            val strategy = NetworkFirstStrategy()

            val result =
                strategy.execute(
                    remote = { error("no network") },
                    local = { listOf("local") },
                    save = { },
                    isExpired = { true },
                )

            assertEquals(listOf("local"), result)
        }
}
