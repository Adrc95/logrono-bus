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

package dev.adrc95.logronobus.core.data.repository

import dev.adrc95.logronobus.core.common.exception.getOrNull
import dev.adrc95.logronobus.core.common.exception.orThrow
import dev.adrc95.logronobus.core.data.DataConstants.NEXT_BUS_WINDOW_MINUTES
import dev.adrc95.logronobus.core.data.departures.FleetSnapshotReader
import dev.adrc95.logronobus.core.data.departures.TimetableRowsReader
import dev.adrc95.logronobus.core.database.datasource.FavouritesDataSource
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.trip.clocksBefore
import dev.adrc95.logronobus.core.domain.trip.departureInstantOf
import dev.adrc95.logronobus.core.domain.trip.directionIndexOf
import dev.adrc95.logronobus.core.domain.trip.headsign
import dev.adrc95.logronobus.core.domain.trip.inDirection
import dev.adrc95.logronobus.core.domain.trip.inOrder
import dev.adrc95.logronobus.core.domain.trip.isSameTrip
import dev.adrc95.logronobus.core.domain.trip.linesBehindFeed
import dev.adrc95.logronobus.core.domain.trip.remember
import dev.adrc95.logronobus.core.domain.trip.toDeparture
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.DeparturesRemoteDataSource
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory(binds = [DeparturesRepository::class])
class DeparturesRepositoryImpl(
    private val remote: DeparturesRemoteDataSource,
    private val catalogue: CatalogueDataSource,
    private val local: FavouritesDataSource,
    private val fleetViews: FleetSnapshotReader,
    private val timetable: TimetableRowsReader,
) : DeparturesRepository {
    override suspend fun learnedDirections(): Map<String, Map<String, String>> = local.getLearnedDirections()

    override suspend fun rememberDirection(
        code: String,
        directionRef: String,
        terminal: String,
    ) {
        persistDirections(
            remember(
                learnedDirections(),
                code,
                directionRef,
                terminal,
            ),
        )
    }

    private suspend fun persistDirections(learned: Map<String, Map<String, String>>) {
        if (learned != local.getLearnedDirections()) {
            local.saveLearnedDirections(learned)
        }
    }

    override suspend fun getLineArrivals(code: String): List<Arrival> {
        val internalId = catalogue.internalIdOf(code) ?: return emptyList()
        return remote.lineArrivals(internalId, listOf(internalId)).orThrow()
    }

    override suspend fun getStopDeparturesWithLive(
        stopId: String,
        now: Instant,
        windowMinutes: Int,
        lineFilter: String?,
        directionFilter: Int?,
    ): StopDepartures {
        val id = stopId.toIntOrNull() ?: return StopDepartures(emptyList())
        val serving = catalogue.internalLinesAt(id)
        if (serving.isEmpty()) return StopDepartures(emptyList())
        val internalLines =
            if (lineFilter == null) {
                serving
            } else {
                catalogue
                    .internalIdOf(lineFilter)
                    ?.takeIf { it in serving }
                    ?.let(::listOf)
                    .orEmpty()
            }
        if (internalLines.isEmpty()) return StopDepartures(emptyList())
        return try {
            departuresAt(id, internalLines, now, windowMinutes, lineFilter, directionFilter)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            StopDepartures(emptyList(), stale = true)
        }
    }

    private suspend fun departuresAt(
        id: Int,
        internalLines: List<Int>,
        now: Instant,
        windowMinutes: Int,
        lineFilter: String?,
        directionFilter: Int?,
    ): StopDepartures {
        val arrivals = remote.arrivals(id, internalLines, windowMinutes).orThrow()
        var learned = learnedDirections()
        val sequences = catalogue.sequences()
        val fleetView =
            fleetViews.snapshotAt(id, internalLines, directionFilter, now, windowMinutes, sequences)
        val departures =
            arrivals
                .mapNotNull { arrival ->
                    val code = catalogue.codeForArrival(arrival.lineRef, id, arrival.order, sequences)
                    val lineSequences = sequences[code].orEmpty()
                    val headsign =
                        headsign(
                            code = code,
                            stopId = id,
                            order = arrival.order,
                            directionRef = arrival.directionRef,
                            sequences = sequences,
                            learned = learned,
                        )
                    val departure =
                        arrival
                            .copy(lineRef = code)
                            .toDeparture(headsign, now, windowMinutes)
                            ?.copy(
                                directionIndex =
                                    directionIndexOf(
                                        lineSequences,
                                        id,
                                        arrival.order,
                                    ),
                            )
                            ?: return@mapNotNull null

                    if (arrival.directionRef.isNotBlank() && headsign != null) {
                        learned = remember(learned, code, arrival.directionRef, headsign)
                    }
                    departure
                }.filter { lineFilter == null || it.timetableId == lineFilter }
                .let { inDirection(it, directionFilter) }
                .let(::inOrder)
        persistDirections(learned)
        val shown = fleetViews.withFleet(departures, fleetView)
        if (departures.isNotEmpty()) return StopDepartures(times = shown)
        val wideArrivals =
            remote.arrivals(id, internalLines, NEXT_BUS_WINDOW_MINUTES).orThrow()
        val later =
            wideArrivals
                .mapNotNull { arrival ->
                    val code = catalogue.codeForArrival(arrival.lineRef, id, arrival.order, sequences)
                    arrival
                        .copy(lineRef = code)
                        .toDeparture(
                            headsign =
                                headsign(
                                    code = code,
                                    stopId = id,
                                    order = arrival.order,
                                    directionRef = arrival.directionRef,
                                    sequences = sequences,
                                    learned = learned,
                                ),
                            now = now,
                            windowMinutes = NEXT_BUS_WINDOW_MINUTES,
                        )?.copy(
                            directionIndex =
                                directionIndexOf(
                                    sequences[code].orEmpty(),
                                    id,
                                    arrival.order,
                                ),
                        )
                }.filter { lineFilter == null || it.timetableId == lineFilter }
                .let { inDirection(it, directionFilter) }
                .let(::inOrder)
        if (later.isNotEmpty()) {
            return StopDepartures(times = fleetViews.withFleet(later, fleetView).take(1))
        }
        val fromTimetable =
            timetable
                .rowsAt(
                    id,
                    internalLines,
                    catalogue.visibleCodes(),
                    now,
                    sequences,
                    learned,
                    directionFilter,
                ).filter { lineFilter == null || it.timetableId == lineFilter }
                .let { inDirection(it, directionFilter) }
                .let(::inOrder)
        return StopDepartures(
            times = fleetViews.withFleet(fromTimetable, fleetView),
        )
    }

    override suspend fun getStopLineStarts(
        stopId: String,
        now: Instant,
        lineFilter: String?,
        directionFilter: Int?,
    ): List<DepartureTime> {
        val id = stopId.toIntOrNull() ?: return emptyList()
        val serving = catalogue.internalLinesAt(id)
        if (serving.isEmpty()) return emptyList()
        val internalLines =
            if (lineFilter == null) {
                serving
            } else {
                catalogue
                    .internalIdOf(lineFilter)
                    ?.takeIf { it in serving }
                    ?.let(::listOf)
                    .orEmpty()
            }
        if (internalLines.isEmpty()) return emptyList()
        val arrivals =
            remote.arrivals(id, internalLines, NEXT_BUS_WINDOW_MINUTES).getOrNull().orEmpty()
        val sequences = catalogue.sequences()
        val learned = learnedDirections()
        val fromFeed =
            arrivals
                .mapNotNull { arrival ->
                    val code = catalogue.codeForArrival(arrival.lineRef, id, arrival.order, sequences)
                    arrival.copy(lineRef = code).toDeparture(
                        headsign =
                            headsign(
                                code = code,
                                stopId = id,
                                order = arrival.order,
                                directionRef = arrival.directionRef,
                                sequences = sequences,
                                learned = learned,
                            ),
                        now = now,
                        windowMinutes = NEXT_BUS_WINDOW_MINUTES,
                    )
                }.filter { lineFilter == null || it.timetableId == lineFilter }
                .let { inDirection(it, directionFilter) }
                .let(::inOrder)
                .distinctBy { it.timetableId }
        val codes = catalogue.visibleCodes()
        val answered = fromFeed.associateBy { it.timetableId }
        val feedAt =
            internalLines
                .mapNotNull { internal ->
                    val live = answered[codes[internal] ?: "$internal"] ?: return@mapNotNull null
                    departureInstantOf(live)?.let { internal to it }
                }.toMap()
        val quiet = linesBehindFeed(internalLines, feedAt, now)
        val fromTimetable =
            when {
                quiet.isEmpty() -> emptyList()
                else -> timetable.rowsAt(id, quiet, codes, now, sequences, learned, directionFilter)
            }.filter { lineFilter == null || it.timetableId == lineFilter }
                .let { inDirection(it, directionFilter) }
                .let(::inOrder)
                .distinctBy { it.timetableId }
        val scheduledByLine = fromTimetable.associateBy { it.timetableId }
        val rows =
            (answered.keys + scheduledByLine.keys).mapNotNull { code ->
                val live = answered[code]
                val scheduled = scheduledByLine[code]
                when {
                    live == null -> scheduled
                    scheduled == null -> live
                    isSameTrip(departureInstantOf(live), departureInstantOf(scheduled)) -> live
                    clocksBefore(departureInstantOf(scheduled), departureInstantOf(live)) -> scheduled
                    else -> live
                }
            }
        return inOrder(rows)
    }
}
