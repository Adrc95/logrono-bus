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

package dev.adrc95.logronobus.core.domain.scan

import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.model.ScannedRow
import dev.adrc95.logronobus.core.model.ScannedSign
import dev.adrc95.logronobus.core.model.ScannedStop
import dev.adrc95.logronobus.core.model.ScannedText
import kotlin.math.abs

fun List<ScannedText>.toScannedSign(): ScannedSign {
    val rows = toRows()
    val stops = rows.stopsColumn()
    val marks = stops.markedStops()
    val lines = lineCodes()
    return ScannedSign(
        line = lines.firstOrNull(),
        lines = lines,
        directions = rows.directionsOf(stops),
        stops = stops.map { ScannedStop(name = it.text, isMarked = it in marks, mark = marks[it] ?: 0) },
    )
}

private fun List<ScannedRow>.markedStops(): Map<ScannedRow, Int> {
    if (size < 2) return emptyMap()
    val scores = map { it.colour }.sorted()
    val typical = scores[scores.size / 2]
    val threshold = maxOf(typical * 2 + DomainConstants.SCAN_MARKED_SHARE, DomainConstants.SCAN_MARKED_COLOUR)
    val best = maxByOrNull { it.colour } ?: return emptyMap()
    if (best.colour < threshold) return emptyMap()
    val second = filter { it !== best }.maxOfOrNull { it.colour } ?: 0
    if (best.colour >= second + DomainConstants.SCAN_MARKED_LEAD) return mapOf(best to best.colour)
    return filter { it.colour >= best.colour - DomainConstants.SCAN_MARKED_BAND }.associateWith { it.colour }
}

private fun List<ScannedRow>.band(
    score: (ScannedRow) -> Int,
    floor: Int,
): Map<ScannedRow, Int> {
    val scores = map { score(it) }.sorted()
    val typical = scores[scores.size / 2]
    val threshold = maxOf(typical * 2 + DomainConstants.SCAN_MARKED_SHARE, floor)
    val best = maxOfOrNull { score(it) } ?: return emptyMap()
    if (best < threshold) return emptyMap()
    return filter { score(it) >= best - DomainConstants.SCAN_MARKED_BAND }.associateWith { score(it) }
}

private fun List<ScannedText>.lineCodes(): List<String> {
    val numbers =
        filter { piece -> piece.text.trim().let { it.length <= 3 && it.all { char -> char.isDigit() } } }
            .sortedByDescending { it.height }
            .take(DomainConstants.SCAN_MAX_LINE_CANDIDATES)
    val night =
        any { it.text.trim().equals(DomainConstants.SCAN_NIGHT_LINE_NAME, ignoreCase = true) } ||
            numbers.any { number ->
                any { it.text.trim().equals("B", ignoreCase = true) && it.overlapsVertically(number) }
            }
    return numbers.map { number -> (if (night) "B" else "") + number.text.trim() }.distinct()
}

private fun List<ScannedRow>.stopsColumn(): List<ScannedRow> {
    if (isEmpty()) return emptyList()
    val heights = map { it.height }.sorted()
    val typical = heights[heights.size / 2]
    if (typical <= 0f) return emptyList()
    val aligned = map { row -> count { abs(it.left - row.left) <= typical * DomainConstants.SCAN_ALIGNMENT } }
    val seed = this[aligned.indices.maxBy { aligned[it] }]
    val ordered = sortedBy { it.top }
    val column = mutableListOf(seed)
    ordered.drop(ordered.indexOf(seed) + 1).forEach { row ->
        val last = column.last()
        if (row.top - last.top > typical * DomainConstants.SCAN_GAP) return@forEach
        if (abs(row.left - last.left) <= typical * DomainConstants.SCAN_DRIFT) column += row
    }
    ordered.take(ordered.indexOf(seed)).reversed().forEach { row ->
        val first = column.first()
        if (first.top - row.top > typical * DomainConstants.SCAN_GAP) return@forEach
        if (abs(row.left - first.left) <= typical * DomainConstants.SCAN_DRIFT) column.add(0, row)
    }
    return column.takeIf { it.size >= DomainConstants.SCAN_MIN_STOPS }.orEmpty()
}

private fun List<ScannedRow>.directionsOf(stops: List<ScannedRow>): List<String> {
    if (stops.isEmpty()) return emptyList()
    val stopsLeft = stops.minOf { it.left }
    val names = filter { it.left < stopsLeft && !it.hasNightName && !it.hasNumber }
    val tallest = names.maxOfOrNull { it.height } ?: return emptyList()
    return names
        .filter { it.height >= tallest * DomainConstants.SCAN_MAJOR_NAME }
        .sortedBy { it.top }
        .take(DomainConstants.SCAN_MAX_DIRECTIONS)
        .glued()
        .map { it.text }
}

private val ScannedRow.hasNumber: Boolean
    get() =
        pieces.any { piece ->
            piece.text.trim().let { it.length <= 3 && it.all { char -> char.isDigit() } }
        }

private val ScannedRow.hasNightName: Boolean
    get() = pieces.any { it.text.trim().equals(DomainConstants.SCAN_NIGHT_LINE_NAME, ignoreCase = true) }

private fun List<ScannedText>.toRows(): List<ScannedRow> {
    val rows = mutableListOf<MutableList<ScannedText>>()
    sortedBy { it.top }.forEach { piece ->
        val row = rows.firstOrNull { row -> row.any { it.sameRowAs(piece) } }
        if (row == null) rows += mutableListOf(piece) else row += piece
    }
    return rows.map { pieces ->
        val ordered = pieces.sortedBy { it.left }
        ScannedRow(
            text = ordered.joinToString(" ") { it.text.trim() }.replace("  ", " ").trim(),
            left = ordered.first().left,
            top = ordered.minOf { it.top },
            height = ordered.maxOf { it.height },
            colour = ordered.maxOf { it.colour },
            dot = ordered.maxOf { it.dot },
            pieces = ordered,
        )
    }
}

private fun List<ScannedRow>.glued(): List<ScannedRow> {
    val glued = mutableListOf<ScannedRow>()
    forEach { row ->
        val previous = glued.lastOrNull()
        val sameColumn =
            previous != null &&
                abs(previous.left - row.left) <= minOf(previous.height, row.height)
        val justUnder =
            previous != null &&
                row.top - (previous.top + previous.height) <=
                minOf(previous.height, row.height) * DomainConstants.SCAN_STACKED_GAP
        if (previous != null && sameColumn && justUnder) {
            glued[glued.lastIndex] =
                previous.copy(
                    text = previous.text + " " + row.text,
                    height = row.top + row.height - previous.top,
                    colour = maxOf(previous.colour, row.colour),
                    dot = maxOf(previous.dot, row.dot),
                )
        } else {
            glued += row
        }
    }
    return glued
}

private fun ScannedText.sameRowAs(other: ScannedText): Boolean {
    val taller = maxOf(height, other.height)
    val shorter = minOf(height, other.height)
    if (taller <= 0f || shorter / taller < DomainConstants.SCAN_SAME_ROW_SIZE) return false
    val shared = minOf(top + height, other.top + other.height) - maxOf(top, other.top)
    if (shared < shorter * DomainConstants.SCAN_SAME_ROW_SHARED) return false
    return gapTo(this, other) <= shorter * DomainConstants.SCAN_SAME_ROW_GAP
}

private fun ScannedText.overlapsVertically(other: ScannedText): Boolean =
    top < other.top + other.height && other.top < top + height

private fun gapTo(
    element: ScannedText,
    other: ScannedText,
): Float {
    val elementRight = element.left + element.width
    val otherRight = other.left + other.width
    return when {
        element.left >= otherRight -> element.left - otherRight
        other.left >= elementRight -> other.left - elementRight
        else -> 0f
    }
}
