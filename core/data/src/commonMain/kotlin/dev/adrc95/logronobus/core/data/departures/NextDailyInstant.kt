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

package dev.adrc95.logronobus.core.data.departures

import dev.adrc95.logronobus.core.common.time.TimeConstants
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun nextDailyInstant(
    time: String,
    now: Instant,
): Instant? {
    val text = time.trim()
    val hour = text.substringBefore(':').toIntOrNull() ?: return null
    val minute = text.substringAfter(':', "").take(2).toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    val today = now.toLocalDateTime(TimeConstants.LOCAL_ZONE).date

    fun at(date: LocalDate) =
        LocalDateTime(
            date,
            LocalTime(hour, minute),
        ).toInstant(TimeConstants.LOCAL_ZONE)
    val first = at(today)
    return if (first >= now) first else at(today.plus(1, DateTimeUnit.DAY))
}
