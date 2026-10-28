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

import dev.adrc95.logronobus.core.domain.departure.hasMeasuredDelay
import dev.adrc95.logronobus.core.domain.departure.isLive
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.model.DepartureWhen
import dev.adrc95.logronobus.core.ui.util.toDepartureWhen
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DepartureDisplayTest {
    private val now = Instant.parse("2026-09-23T17:00:00Z")
    private val zone = TimeZone.UTC

    private fun whenIn(offsetSeconds: Long) = (now + offsetSeconds.seconds).toDepartureWhen(now, zone)

    @Test
    fun `given a departure inside the last minute when it is mapped then it reads as due`() {
        assertEquals(DepartureWhen.Due, whenIn(29))
        assertEquals(DepartureWhen.Due, whenIn(59))
        assertEquals(
            DepartureWhen.Due,
            whenIn(-45),
            "Just gone but still listed reads as due, not negative",
        )
        assertEquals(DepartureWhen.Minutes(1), whenIn(60), "A whole minute left is the first one shown")
    }

    @Test
    fun `given a nearby departure when it is mapped then it counts the whole minutes left`() {
        assertEquals(DepartureWhen.Minutes(1), whenIn(89))
        assertEquals(DepartureWhen.Minutes(1), whenIn(90), "A minute and a half left is one minute, never two")
        assertEquals(DepartureWhen.Minutes(19), whenIn(19.minutes.inWholeSeconds + 59))
    }

    @Test
    fun `given a departure twenty minutes out when it is mapped then it is shown as a clock time`() {
        assertEquals(DepartureWhen.ClockTime("17:20"), whenIn(20.minutes.inWholeSeconds))
        assertEquals(
            DepartureWhen.Minutes(19),
            whenIn(20.minutes.inWholeSeconds - 1),
            "One second short of twenty still counts in minutes",
        )
        assertEquals(DepartureWhen.ClockTime("18:12"), whenIn(72.minutes.inWholeSeconds))
    }

    @Test
    fun `given a departure when its clock time is shown then it is the rider's local time`() {
        val madrid = TimeZone.of("Europe/Madrid")
        assertEquals(
            DepartureWhen.ClockTime("19:42"),
            Instant.parse("2026-09-23T17:42:00Z").toDepartureWhen(now, madrid),
        )
        assertEquals(
            DepartureWhen.ClockTime("18:42"),
            Instant.parse("2026-12-23T17:42:00Z").toDepartureWhen(now, madrid),
        )
    }

    @Test
    fun `given a departure for a later day when it is mapped then it says so`() {
        assertEquals(false, (whenIn(72.minutes.inWholeSeconds) as DepartureWhen.ClockTime).nextDay)
        val tomorrow = Instant.parse("2026-09-24T05:00:00Z").toDepartureWhen(now, zone) as DepartureWhen.ClockTime
        assertEquals("05:00", tomorrow.time)
        assertTrue(tomorrow.nextDay, "the next service day, not today's 05:00")
    }

    @Test
    fun `given a missing time when it is mapped then it is unknown`() {
        assertEquals(DepartureWhen.Unknown, null.toDepartureWhen(now, zone))
    }

    @Test
    fun `given a departure the feed reported when it is mapped then it is live`() {
        fun dep(delay: Int?) = DepartureTime(displayName = "City", timetableId = "401", delaySeconds = delay)
        assertTrue(dep(0).isLive, "On time according to the feed is still live")
        assertTrue(dep(240).isLive)
        assertFalse(dep(null).isLive, "No delay reading means timetable only")
    }

    @Test
    fun `given a timetable row without a bus when it is mapped then it is not followed`() {
        fun dep(status: String) =
            DepartureTime(
                displayName = "City",
                timetableId = "401",
                delaySeconds = 0,
                arrivalStatus = status,
            )
        assertFalse(dep("NO_REPORT").isLive, "NO_REPORT is the timetable speaking")
        assertFalse(dep("SCHEDULED").isLive)
    }

    @Test
    fun `given a row the operator calls an estimate when it is mapped then it claims no delay`() {
        fun dep(
            status: String,
            delay: Int?,
        ) = DepartureTime(
            displayName = "City",
            timetableId = "401",
            delaySeconds = delay,
            arrivalStatus = status,
        )
        assertFalse(dep("NO_REPORT", -780).hasMeasuredDelay)
        assertFalse(dep("SCHEDULED", 240).hasMeasuredDelay)
        assertTrue(dep("MONITORED", 240).hasMeasuredDelay)
        assertFalse(dep("MONITORED", 240).copy(predictionInaccurate = true).hasMeasuredDelay)
    }
}
