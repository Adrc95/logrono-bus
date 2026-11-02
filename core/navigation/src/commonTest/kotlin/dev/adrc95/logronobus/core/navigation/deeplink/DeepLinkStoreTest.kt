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

package dev.adrc95.logronobus.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private data object StoreKey : NavKey

class DeepLinkStoreTest {
    @Test
    fun `given a link when it is submitted then it is pending`() {
        val store = DeepLinkStore()

        store.submit("logronobus://routes?line=6")

        assertEquals("logronobus://routes?line=6", store.pending.value)
    }

    @Test
    fun `given no link when it is submitted then nothing is pending`() {
        val store = DeepLinkStore()

        store.submit(null)

        assertNull(store.pending.value)
    }

    @Test
    fun `given a pending link when it is cleared then nothing is pending`() {
        val store = DeepLinkStore()
        store.submit("logronobus://routes?line=6")

        store.clear()

        assertNull(store.pending.value)
    }

    @Test
    fun `given a pendingMatch link when it is published then the screen can read it`() {
        val store = DeepLinkStore()
        val match = DeepLinkMatch(StoreKey, mapOf("line" to listOf("6")))

        store.setPendingMatch(match)

        assertEquals(match, store.pendingMatch.value)
    }

    @Test
    fun `given a published link when it is cleared then nothing is left for the screen`() {
        val store = DeepLinkStore()
        store.setPendingMatch(DeepLinkMatch(StoreKey, emptyMap()))

        store.clearPendingMatch()

        assertNull(store.pendingMatch.value)
    }
}
