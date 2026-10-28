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

package dev.adrc95.logronobus.core.testing

import dev.adrc95.logronobus.core.common.time.TimeConstants
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.toLocalStamp(): String {
    val local = toLocalDateTime(TimeConstants.LOCAL_ZONE)

    val offsetMinutes =
        ((local.toInstant(TimeZone.UTC).epochSeconds - epochSeconds) / 60).toInt()
    val sign = if (offsetMinutes < 0) '-' else '+'
    val offset = kotlin.math.abs(offsetMinutes)

    fun pad(value: Int): String = value.toString().padStart(2, '0')
    return "${local.year}-${pad(local.month.number)}-${pad(local.day)}T" +
        "${pad(local.hour)}:${pad(local.minute)}:${pad(local.second)}" +
        "$sign${pad(offset / 60)}:${pad(offset % 60)}"
}
