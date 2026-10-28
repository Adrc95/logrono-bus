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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopSequence
import kotlin.time.Instant

fun isNightLine(code: String): Boolean = code.startsWith("B")

fun offersTonight(
    code: String,
    nightLines: Set<String>,
    runningTonight: Set<String>,
): Boolean = code !in nightLines || code in runningTonight

fun servedDirections(
    sequences: List<StopSequence>,
    stopId: Int,
    direction: Int?,
): List<Int?> =
    when {
        direction != null -> {
            if (sequences.getOrNull(direction)?.ids?.contains(stopId) == true) {
                listOf(direction)
            } else {
                emptyList()
            }
        }

        else -> {
            sequences.indices
                .filter { stopId in sequences[it].ids }
                .ifEmpty { listOf(null) }
        }
    }

fun nextPerLine(rows: List<DepartureTime>): List<DepartureTime> =
    rows
        .groupBy { it.timetableId }
        .mapNotNull { (_, line) -> line.minByOrNull { it.departTimestamp ?: Instant.DISTANT_FUTURE } }

fun offeredInDirection(
    rows: List<DepartureTime>,
    direction: Int?,
): List<DepartureTime> =
    rows.filter { direction == null || it.directionIndex == null || it.directionIndex == direction }
