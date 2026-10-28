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
import dev.adrc95.logronobus.core.model.ScannedSign
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopSignReading

fun resolveDirection(
    sign: ScannedSign,
    stopsByDirection: List<List<Stop>>,
    origins: List<String>,
): Int? {
    val names = sign.stops.map { it.name }
    if (names.size > 1) {
        val fits = stopsByDirection.map { stops -> howItFits(names, stops) }
        val best = fits.maxOrNull() ?: 0
        val winners = fits.indices.filter { fits[it] == best }
        if (best >= DomainConstants.SCAN_MIN_FIT && winners.size == 1) return winners.first()
    }
    val first = sign.stops.firstOrNull()?.name
    val last = sign.stops.lastOrNull()?.name
    if (last != null) {
        val byLast =
            stopsByDirection.indices.filter { index ->
                val directionLast = stopsByDirection[index].lastOrNull()?.longName
                directionLast != null && namesMatch(directionLast, last)
            }
        if (byLast.size == 1) return byLast.first()
    }
    if (first != null) {
        val byFirst =
            stopsByDirection.indices.filter { index ->
                val directionFirst = stopsByDirection[index].firstOrNull()?.longName
                directionFirst != null && namesMatch(directionFirst, first)
            }
        if (byFirst.size == 1) return byFirst.first()
    }
    if (names.isNotEmpty()) {
        val byName =
            origins.indices.filter { index ->
                val origin = origins[index]
                origin.isNotBlank() && names.any { name -> namesMatch(origin, name) }
            }
        if (byName.size == 1) return byName.first()
    }
    return null
}

private fun howItFits(
    sign: List<String>,
    stops: List<Stop>,
): Int {
    if (stops.isEmpty()) return 0
    val names = stops.map { it.longName }
    var previous = IntArray(names.size + 1)
    var current = IntArray(names.size + 1)
    sign.forEach { scanned ->
        current[0] = 0
        names.forEachIndexed { index, name ->
            current[index + 1] =
                if (namesMatch(name, scanned)) {
                    previous[index] + 1
                } else {
                    maxOf(current[index], previous[index + 1])
                }
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[names.size]
}

fun findMarkedStop(
    sign: ScannedSign,
    candidates: List<Stop>,
): Stop? {
    val marked = sign.stops.filter { it.isMarked }.maxByOrNull { it.mark } ?: return null
    return candidates.firstOrNull {
        namesMatch(it.longName, marked.name) ||
            namesMatch(it.shortName, marked.name)
    }
}

fun namesMatch(
    catalogueName: String,
    signName: String,
): Boolean {
    val catalogue = normalizeName(catalogueName)
    val sign = normalizeName(signName)
    if (catalogue.isEmpty() || sign.isEmpty()) return false
    if (catalogue == sign) return true
    val shorter = minOf(catalogue.length, sign.length)
    val longer = maxOf(catalogue.length, sign.length)
    if (shorter >= 4 && catalogue.contains(sign)) return true
    if (shorter >= 4 && sign.contains(catalogue) &&
        sign.length * 10 <= catalogue.length * DomainConstants.SCAN_LONGER_SIGN_PERCENT
    ) {
        return true
    }
    if (sameWords(catalogue, sign)) return true
    if (shorter * 100 / longer < DomainConstants.SCAN_MIN_SHARED_PERCENT) return false
    return edits(catalogue, sign) * 100 <= longer * DomainConstants.SCAN_MISREAD_PERCENT
}

private fun sameWords(
    first: String,
    second: String,
): Boolean {
    val firstWords = first.split(" ").filter { it.isNotBlank() }.sorted()
    val secondWords = second.split(" ").filter { it.isNotBlank() }.sorted()
    return firstWords == secondWords
}

private fun edits(
    first: String,
    second: String,
): Int {
    var previous = IntArray(second.length + 1) { it }
    var current = IntArray(second.length + 1)
    first.forEachIndexed { index, char ->
        current[0] = index + 1
        second.forEachIndexed { position, other ->
            val replace = previous[position] + if (char == other) 0 else 1
            current[position + 1] =
                minOf(current[position] + 1, previous[position + 1] + 1, replace)
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[second.length]
}

private fun normalizeName(name: String): String =
    name
        .lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ñ", "n")
        .filter { it.isLetterOrDigit() || it == ' ' }
        .replace(Regex("\\s+"), " ")
        .trim()

fun ScannedSign.howManyAreRead(candidates: List<Stop>): Int =
    stops.count { scanned ->
        candidates.any { stop ->
            namesMatch(stop.longName, scanned.name) ||
                namesMatch(stop.shortName, scanned.name)
        }
    }

fun StopSignReading.keepingTheGoodReading(previous: StopSignReading?): StopSignReading =
    if (previous != null && previous.line == line && previous.stop != null) previous else this
