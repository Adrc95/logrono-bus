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

package dev.adrc95.logronobus.core.domain.fleet

import dev.adrc95.logronobus.core.model.BusPositions
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsMeaningfullyStaleTest {
    private val threshold = 90L

    private fun positions(
        stale: Boolean,
        staleSeconds: Long?,
    ) = BusPositions(byRoute = emptyMap(), stale = stale, staleSeconds = staleSeconds)

    @Test
    fun liveDataIsNeverFlagged() {
        assertFalse(positions(stale = false, staleSeconds = null).isMeaningfullyStale)
        assertFalse(positions(stale = false, staleSeconds = 500L).isMeaningfullyStale)
    }

    @Test
    fun aFreshFallbackUnderThresholdIsNotFlagged() {
        assertFalse(positions(stale = true, staleSeconds = 10L).isMeaningfullyStale)
        assertFalse(positions(stale = true, staleSeconds = threshold - 1).isMeaningfullyStale)
    }

    @Test
    fun aFallbackAtOrPastThresholdIsFlagged() {
        assertTrue(positions(stale = true, staleSeconds = threshold).isMeaningfullyStale)
        assertTrue(positions(stale = true, staleSeconds = threshold + 120).isMeaningfullyStale)
    }

    @Test
    fun anUnknownAgeIsTreatedAsWorthFlagging() {
        assertTrue(positions(stale = true, staleSeconds = null).isMeaningfullyStale)
    }
}
