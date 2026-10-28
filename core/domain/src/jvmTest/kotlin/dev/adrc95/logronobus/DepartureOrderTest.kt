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

import dev.adrc95.logronobus.core.domain.trip.clocksBefore
import dev.adrc95.logronobus.core.domain.trip.isSameTrip
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class DepartureOrderTest {
    private val twentyTwo = Instant.parse("2026-10-20T22:00:00Z")
    private val fourMinutesLater = Instant.parse("2026-10-20T22:04:00Z")
    private val tenMinutesLater = Instant.parse("2026-10-20T22:10:00Z")

    @Test
    fun `given two clocks minutes apart when they are compared then they are the same trip`() {
        assertTrue(isSameTrip(twentyTwo, fourMinutesLater), "the feed and the timetable disagree by minutes")
        assertTrue(isSameTrip(fourMinutesLater, twentyTwo), "the order of the two sources does not matter")
    }

    @Test
    fun `given two clocks a line apart when they are compared then they are different trips`() {
        assertFalse(isSameTrip(twentyTwo, tenMinutesLater), "buses on a line are ten minutes apart")
    }

    @Test
    fun `given a row without a clock when it is compared then it is never the same trip`() {
        assertFalse(isSameTrip(null, twentyTwo))
        assertFalse(isSameTrip(twentyTwo, null))
    }

    @Test
    fun `given two rows when they are ordered then only named instants are compared`() {
        assertTrue(clocksBefore(twentyTwo, tenMinutesLater))
        assertFalse(clocksBefore(tenMinutesLater, twentyTwo))
        assertFalse(clocksBefore(null, twentyTwo), "an unknown clock cannot be ordered")
    }
}
