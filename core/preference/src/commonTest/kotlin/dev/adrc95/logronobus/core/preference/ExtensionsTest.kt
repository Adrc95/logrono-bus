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

package dev.adrc95.logronobus.core.preference

import dev.adrc95.logronobus.core.testing.fake.FakePreferencesStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionsTest {
    private val store = FakePreferencesStore()

    @Test
    fun `given a missing key when it is read then the default comes back`() =
        runTest {
            assertEquals("", store.get("language", ""))
            assertEquals(0, store.get("count", 0))
        }

    @Test
    fun `given a written value when it is read then the last written value comes back`() =
        runTest {
            store.set("language", "eu")
            assertEquals("eu", store.get("language", ""))
            store.set("language", "es")
            assertEquals("es", store.get("language", ""))
        }

    @Test
    fun `given a written value when null is written then the key is removed`() =
        runTest {
            store.set("language", "eu")
            store.set<String>("language", null)
            assertEquals("", store.get("language", ""))
        }

    @Test
    fun `given a written value when the key is removed then the default comes back`() =
        runTest {
            store.set("theme", "dark")
            store.remove<String>("theme")
            assertEquals("light", store.get("theme", "light"))
        }

    @Test
    fun `given an observed key when it changes then the default and every change are emitted`() =
        runTest {
            val seen = mutableListOf<String>()
            val job =
                launch {
                    store
                        .observe("language", "")
                        .take(2)
                        .toList(seen)
                }
            runCurrent()

            store.set("language", "eu")
            job.join()

            assertEquals(listOf("", "eu"), seen)
        }

    @Test
    fun `given every supported type when it is written and read then the value round trips`() =
        runTest {
            store.set("string", "value")
            store.set("int", 7)
            store.set("long", 7L)
            store.set("float", 1.5f)
            store.set("double", 1.5)
            store.set("boolean", true)
            store.set("set", setOf("a", "b"))
            assertEquals("value", store.get("string", ""))
            assertEquals(7, store.get("int", 0))
            assertEquals(7L, store.get("long", 1L))
            assertEquals(1.5f, store.get("float", 0f))
            assertEquals(1.5, store.get("double", 0.0))
            assertEquals(true, store.get("boolean", false))
            assertEquals(setOf("a", "b"), store.get("set", emptySet<String>()))
        }

    @Test
    fun `given an unsupported type when the key is built then it fails`() =
        runTest {
            val failure =
                assertFailsWith<IllegalStateException> {
                    store.get("key", NotStorable())
                }
            assertEquals("A preference cannot hold a NotStorable", failure.message)
        }
}

private class NotStorable
