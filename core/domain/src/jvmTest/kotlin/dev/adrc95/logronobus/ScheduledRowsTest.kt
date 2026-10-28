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

import dev.adrc95.logronobus.core.domain.trip.isBoardable
import dev.adrc95.logronobus.core.domain.trip.linesBehindFeed
import dev.adrc95.logronobus.core.domain.trip.unansweredLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ScheduledRowsTest {
    private val now = Instant.parse("2026-10-20T22:00:00Z")

    private fun at(offset: kotlin.time.Duration) = now + offset

    @Test
    fun `given a pass a minute in the past when the rows are built then it is gone`() {
        assertTrue(isBoardable(at(-30.seconds), now, 90))
        assertFalse(isBoardable(at(-61.seconds), now, 90))
    }

    @Test
    fun `given a pass exactly a minute old when the rows are built then it still shows`() {
        assertTrue(isBoardable(at(-60.seconds), now, 90))
    }

    @Test
    fun `given a pass for tomorrow when the rows are built then it is not taken as tonight's`() {
        assertTrue(isBoardable(at(89.minutes), now, 90))
        assertTrue(isBoardable(at(90.minutes), now, 90))
        assertFalse(isBoardable(at(91.minutes), now, 90))
    }

    @Test
    fun `given a line without a pass when the rows are built then it is owed one in the stop's own order`() {
        val codes = mapOf(4 to "4", 7 to "7", 9 to "9")
        val owed =
            unansweredLines(
                internalIds = listOf(9, 7, 4),
                codes = codes,
                answered = setOf("7"),
                offered = setOf("4", "7", "9"),
            )

        assertEquals(listOf(9 to "9", 4 to "4"), owed)
    }

    @Test
    fun `given a line that does not run tonight when the rows are built then it is owed nothing`() {
        val codes = mapOf(4 to "N4")
        val owed = unansweredLines(listOf(4), codes, emptySet(), offered = emptySet())

        assertEquals(emptyList(), owed)
    }

    @Test
    fun `given a line the catalogue cannot name when the rows are built then it is skipped`() {
        val owed = unansweredLines(listOf(4, 5), mapOf(5 to "5"), emptySet(), setOf("5"))

        assertEquals(listOf(5 to "5"), owed)
    }

    @Test
    fun `given some lines in the feed when the rows are built then only the missing ones get the timetable`() {
        val feedAt = mapOf(9 to at(2.minutes), 7 to at(6.minutes))

        assertEquals(listOf(7, 4), linesBehindFeed(listOf(9, 7, 4), feedAt, now))
    }

    @Test
    fun `given a pass five minutes ahead when the rows are built then it is still the feed's`() {
        assertEquals(emptyList(), linesBehindFeed(listOf(9), mapOf(9 to at(5.minutes)), now))
        assertEquals(listOf(9), linesBehindFeed(listOf(9), mapOf(9 to at(5.minutes) + 1.seconds), now))
    }

    @Test
    fun `given a line with an unreadable time when the rows are built then it is owed a departure rather than hidden`() {
        assertEquals(listOf(9), linesBehindFeed(listOf(9), emptyMap(), now))
    }
}
