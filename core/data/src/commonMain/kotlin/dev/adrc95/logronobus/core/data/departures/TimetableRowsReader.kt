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

import dev.adrc95.logronobus.core.common.exception.getOrNull
import dev.adrc95.logronobus.core.common.exception.orThrow
import dev.adrc95.logronobus.core.data.DataConstants.NEXT_BUS_WINDOW_MINUTES
import dev.adrc95.logronobus.core.domain.trip.directionIndexOf
import dev.adrc95.logronobus.core.domain.trip.headsign
import dev.adrc95.logronobus.core.domain.trip.isBoardable
import dev.adrc95.logronobus.core.domain.trip.isNightLine
import dev.adrc95.logronobus.core.domain.trip.minutesFromStartTo
import dev.adrc95.logronobus.core.domain.trip.nextPerLine
import dev.adrc95.logronobus.core.domain.trip.offeredInDirection
import dev.adrc95.logronobus.core.domain.trip.offersTonight
import dev.adrc95.logronobus.core.domain.trip.servedDirections
import dev.adrc95.logronobus.core.domain.trip.unansweredLines
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.DeparturesRemoteDataSource
import dev.adrc95.logronobus.core.network.datasource.FleetDataSource
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Factory
class TimetableRowsReader(
    private val remote: DeparturesRemoteDataSource,
    private val catalogue: CatalogueDataSource,
    private val fleet: FleetDataSource,
) {
    suspend fun rowsAt(
        stopId: Int,
        internalIds: List<Int>,
        codes: Map<Int, String>,
        now: Instant,
        sequences: Map<String, List<StopSequence>>,
        learned: Map<String, Map<String, String>>,
        direction: Int? = null,
    ): List<DepartureTime> {
        val nightLines = codes.values.filterTo(mutableSetOf()) { isNightLine(it) }
        val nightLinesOut =
            if (nightLines.isEmpty()) {
                emptySet()
            } else {
                fleet
                    .positions(now, forceRefresh = false)
                    .getOrNull()
                    ?.all
                    .orEmpty()
                    .mapNotNull { it.timetableId }
                    .toSet()
            }

        fun runsTonight(code: String) =
            offersTonight(
                code,
                nightLines,
                nightLinesOut,
            )
        val offered = codes.values.filterTo(mutableSetOf()) { runsTonight(it) }
        val passes = remote.passes(stopId, internalIds).orThrow()
        val scheduled =
            passes
                .mapNotNull { pass ->
                    val code = catalogue.codeForArrival(pass.lineRef, stopId, pass.order, sequences)
                    if (code.isEmpty() || code !in offered) return@mapNotNull null
                    val at = pass.aimedArrival ?: pass.aimedDeparture ?: return@mapNotNull null
                    if (!isBoardable(at, now, NEXT_BUS_WINDOW_MINUTES)) {
                        return@mapNotNull null
                    }
                    DepartureTime(
                        displayName =
                            headsign(
                                code = code,
                                stopId = stopId,
                                order = pass.order,
                                directionRef = pass.directionRef,
                                sequences = sequences,
                                learned = learned,
                            ).orEmpty(),
                        timetableId = code,
                        departTimestamp = at,
                        delaySeconds = null,
                        arrivalStatus = "scheduled",
                        directionIndex =
                            directionIndexOf(
                                sequences[code].orEmpty(),
                                stopId,
                                pass.order,
                            ),
                    )
                }.let { offeredInDirection(it, direction) }
                .let(::nextPerLine)
        val answered = scheduled.mapTo(mutableSetOf()) { it.timetableId }
        val stillQuiet = unansweredLines(internalIds, codes, answered, offered)
        val routes = catalogue.getRoutes()
        val tomorrow =
            stillQuiet
                .flatMap { (internal, code) ->
                    val lineSequences = sequences[code].orEmpty()
                    val served = servedDirections(lineSequences, stopId, direction)
                    val linePasses =
                        remote.linePasses(internal).getOrNull()
                            ?: return@flatMap emptyList()
                    served.mapNotNull { index ->
                        val passes =
                            linePasses.byDirection[directionKey(lineSequences, index)]
                                ?: linePasses.overall
                        if (passes.isEmpty()) return@mapNotNull null
                        val start =
                            passes.mapNotNull { nextDailyInstant(it, now) }.minOrNull()
                                ?: return@mapNotNull null
                        val ride =
                            index?.let { direction ->
                                catalogue
                                    .getStopsForRoute(code)
                                    .getOrNull(direction)
                                    ?.minutesFromStartTo(stopId.toString())
                                    ?: 0
                            } ?: 0
                        val at = start.plus(ride.minutes)
                        val plainName =
                            routes[code]
                                ?.longName
                                .orEmpty()
                                .split("-")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .drop(1)
                                .firstOrNull()
                                .orEmpty()
                        DepartureTime(
                            displayName =
                                index?.let {
                                    catalogue.directionNameFor(code, it)
                                } ?: plainName,
                            timetableId = code,
                            departTimestamp = at,
                            delaySeconds = null,
                            arrivalStatus = "scheduled",
                            directionIndex = index ?: direction,
                        )
                    }
                }.let(::nextPerLine)
        return scheduled + tomorrow
    }

    private fun directionKey(
        sequences: List<StopSequence>,
        index: Int?,
    ): String? =
        when {
            index == null -> null
            sequences.size < 2 -> "Ida"
            index == 0 -> "Ida"
            index == sequences.lastIndex -> "Vuelta"
            else -> null
        }
}
