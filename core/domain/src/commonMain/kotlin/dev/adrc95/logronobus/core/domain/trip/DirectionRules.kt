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

import dev.adrc95.logronobus.core.model.StopSequence

internal fun terminalFor(
    sequences: List<StopSequence>,
    stopId: Int,
    order: Int?,
): String? {
    if (order == null || order < 1) return null
    val terminals =
        sequences
            .filter { it.ids.indexOf(stopId) + 1 == order }
            .mapNotNull { it.terminal }
            .filter { it.isNotBlank() }
            .distinct()
    return terminals.singleOrNull()
}

fun directionIndexOf(
    sequences: List<StopSequence>,
    stopId: Int,
    order: Int?,
): Int? {
    if (order == null || order < 1) return null
    val matches =
        sequences.indices.filter { index ->
            sequences[index].ids.indexOf(stopId) + 1 == order
        }
    return matches.singleOrNull()
}

fun headsign(
    code: String,
    stopId: Int,
    order: Int?,
    directionRef: String,
    sequences: Map<String, List<StopSequence>>,
    learned: Map<String, Map<String, String>>,
): String? =
    terminalFor(sequences[code].orEmpty(), stopId, order)
        ?: learned[code]?.get(directionRef)?.takeIf { it.isNotBlank() }

fun remember(
    learned: Map<String, Map<String, String>>,
    code: String,
    directionRef: String,
    terminal: String,
): Map<String, Map<String, String>> {
    if (directionRef.isBlank()) return learned
    val forCode = learned[code].orEmpty() + (directionRef to terminal)
    return learned + (code to forCode)
}
