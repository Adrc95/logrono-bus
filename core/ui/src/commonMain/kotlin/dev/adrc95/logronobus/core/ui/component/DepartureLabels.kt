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

package dev.adrc95.logronobus.core.ui.component

import androidx.compose.runtime.Composable
import dev.adrc95.logronobus.core.common.time.TimeConstants
import dev.adrc95.logronobus.core.domain.departure.hasMeasuredDelay
import dev.adrc95.logronobus.core.domain.trip.minutesUntilFrom
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.countdown_min
import dev.adrc95.logronobus.core.ui.generated.resources.due
import dev.adrc95.logronobus.core.ui.generated.resources.early_suffix
import dev.adrc95.logronobus.core.ui.generated.resources.late_suffix
import dev.adrc95.logronobus.core.ui.generated.resources.not_available
import dev.adrc95.logronobus.core.ui.generated.resources.tomorrow_at
import dev.adrc95.logronobus.core.ui.model.DepartureWhen
import dev.adrc95.logronobus.core.ui.util.toDepartureWhen
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun DepartureTime.countdownLabel(nowMs: Long): String =
    when (
        val at =
            departTimestamp.toDepartureWhen(
                Instant.fromEpochMilliseconds(nowMs),
                TimeConstants.LOCAL_ZONE,
            )
    ) {
        DepartureWhen.Due -> {
            stringResource(Res.string.due)
        }

        is DepartureWhen.Minutes -> {
            stringResource(Res.string.countdown_min, at.minutes)
        }

        is DepartureWhen.ClockTime -> {
            if (at.nextDay) {
                stringResource(Res.string.tomorrow_at, at.time)
            } else {
                at.time
            }
        }

        DepartureWhen.Unknown -> {
            stringResource(Res.string.not_available)
        }
    }

@Composable
fun DepartureTime.remainingMinutesLabel(nowMs: Long): String = remainingMinutesLabel(departTimestamp, nowMs)

@Composable
fun remainingMinutesLabel(
    departTimestamp: Instant?,
    nowMs: Long,
): String {
    val at = departTimestamp ?: return stringResource(Res.string.not_available)
    val minutes = at.minutesUntilFrom(Instant.fromEpochMilliseconds(nowMs))
    return if (minutes <= 0) {
        stringResource(Res.string.due)
    } else {
        stringResource(Res.string.countdown_min, minutes)
    }
}

@Composable
fun DepartureTime.departureLabel(nowMs: Long): String {
    val countdown = countdownLabel(nowMs)
    if (!hasMeasuredDelay) return countdown
    val delaySeconds = delaySeconds ?: return countdown
    val suffix =
        when {
            delaySeconds > 30 -> stringResource(Res.string.late_suffix)
            delaySeconds < -30 -> stringResource(Res.string.early_suffix)
            else -> ""
        }
    return "$countdown$suffix"
}
