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

package dev.adrc95.logronobus.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.time_hours_ago
import dev.adrc95.logronobus.core.ui.generated.resources.time_just_now
import dev.adrc95.logronobus.core.ui.generated.resources.time_mins_ago
import dev.adrc95.logronobus.core.ui.generated.resources.time_secs_ago
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Composable
inline fun <reified T : Enum<T>> rememberSaveableEnum(initial: T): MutableState<T> =
    rememberSaveable(
        saver =
            Saver<MutableState<T>, String>(
                save = { it.value.name },
                restore = { saved -> mutableStateOf(enumValueOf<T>(saved)) },
            ),
    ) { mutableStateOf(initial) }

@Composable
fun rememberNowMs(intervalMillis: Long = NOW_TICK_MILLIS): Long {
    var nowMs by remember { mutableLongStateOf(nowEpochMilliseconds()) }
    LaunchedEffect(intervalMillis) {
        while (true) {
            delay(intervalMillis.milliseconds)
            nowMs = nowEpochMilliseconds()
        }
    }
    return nowMs
}

@Composable
fun timeAgoIsoLabel(
    isoTimestamp: String,
    nowMs: Long,
): String {
    val thenMs =
        try {
            Instant.parse(isoTimestamp).toEpochMilliseconds()
        } catch (_: Exception) {
            return ""
        }
    return timeAgoLabel(thenMs, nowMs)
}

@Composable
fun timeAgoLabel(
    thenMs: Long,
    nowMs: Long,
): String {
    val diffSecs = (nowMs - thenMs) / 1000
    return when {
        diffSecs < 10 -> stringResource(Res.string.time_just_now)
        diffSecs < 60 -> stringResource(Res.string.time_secs_ago, diffSecs.toInt())
        diffSecs < 3600 -> stringResource(Res.string.time_mins_ago, (diffSecs / 60).toInt())
        else -> stringResource(Res.string.time_hours_ago, (diffSecs / 3600).toInt())
    }
}
