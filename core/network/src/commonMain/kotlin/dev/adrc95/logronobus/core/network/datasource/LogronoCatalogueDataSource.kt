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

package dev.adrc95.logronobus.core.network.datasource

import dev.adrc95.logronobus.core.common.exception.orThrow
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.NetworkConstants.NO_LINE
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto
import dev.adrc95.logronobus.core.network.dto.StopDto
import dev.adrc95.logronobus.core.network.mapper.directionHeadsigns
import dev.adrc95.logronobus.core.network.mapper.directionShapes
import dev.adrc95.logronobus.core.network.mapper.directionStops
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.mapper.visibleCode
import dev.adrc95.logronobus.core.network.service.CatalogueService
import dev.adrc95.logronobus.core.network.tryCall
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single(binds = [CatalogueDataSource::class])
class LogronoCatalogueDataSource(
    private val service: CatalogueService,
) : CatalogueDataSource {
    private val staticMutex = Mutex()
    private var stopsRawCache: List<StopDto>? = null
    private var stopsCache: List<Stop>? = null
    private var lineCodesCache: Map<Int, String>? = null
    private var linesCache: List<LineDto>? = null
    private var routesCache: Map<String, Route>? = null
    private var sequencesCache: Map<String, List<StopSequence>>? = null
    private val routeStopsCache = mutableMapOf<String, List<List<Stop>>>()
    private var stopRefsByDirectionCache: Map<String, List<Set<String>>>? = null
    private val routeHeadsignsCache = mutableMapOf<String, List<String>>()
    private val routeLabelsCache = mutableMapOf<String, List<String>>()
    private val routeOriginsCache = mutableMapOf<String, List<String>>()
    private val stopLabelsCache = mutableMapOf<String, Map<String, String>>()
    private val stopLinesCache = mutableMapOf<Int, List<Int>>()
    private val routeShapesCache = mutableMapOf<String, List<List<GeoPoint>>>()

    override suspend fun getRoutes(): Map<String, Route> = staticMutex.withLock { routesLocked() }

    override suspend fun getStops(): List<Stop> = staticMutex.withLock { stopsLocked() }

    private suspend fun routesLocked(): Map<String, Route> =
        routesCache ?: linesLocked()
            .map { it.toDomain() }
            .associateBy { it.timetableId }
            .also { routesCache = it }

    private suspend fun stopsLocked(): List<Stop> =
        stopsCache ?: stopsRawLocked()
            .map { it.toDomain(lineCodesLocked()) }
            .also { stopsCache = it }

    private suspend fun stopsRawLocked(): List<StopDto> =
        stopsRawCache ?: tryCall { service.getStops() }
            .orThrow()
            .result.stops
            .also { stopsRawCache = it }

    private suspend fun linesLocked(): List<LineDto> =
        linesCache ?: tryCall { service.getLines() }
            .orThrow()
            .result.lines
            .also { fetched ->
                lineCodesCache = lineCodes(fetched)
                linesCache = fetched
            }

    private suspend fun lineCodesLocked(): Map<Int, String> =
        lineCodesCache ?: lineCodes(linesLocked()).also { lineCodesCache = it }

    override suspend fun sequences(): Map<String, List<StopSequence>> =
        staticMutex.withLock {
            sequencesCache ?: linesLocked()
                .sequences()
                .also { sequencesCache = it }
        }

    override suspend fun internalLinesAt(id: Int): List<Int> =
        staticMutex.withLock {
            stopLinesCache[id]?.let { return@withLock it }
            val fromSequences =
                linesLocked()
                    .filter { line ->
                        line.stops.asc.any { it.id == id } || line.stops.desc.any { it.id == id }
                    }.map { it.id }
            val fromCatalogue = stopsRawLocked().firstOrNull { it.id == id }?.lines.orEmpty()
            val ids = (fromCatalogue + fromSequences).distinct()
            stopLinesCache[id] = ids
            ids
        }

    private val learnedLineAliases = mutableMapOf<Int, String>()

    private suspend fun lineCodeFor(lineRef: String): String {
        val id = lineRef.toIntOrNull()
        if (id != null) {
            visibleCodes()[id]?.let { return it }
            learnedLineAliases[id]?.let { return it }
        }
        return visibleCode(lineRef)
    }

    override suspend fun fleetCodeResolver(): (String, String?) -> String? {
        val codes = visibleCodes()
        val byNameBody =
            linesLocked().associate { line ->
                line.name
                    .substringAfter('-')
                    .trim()
                    .uppercase() to visibleCode(line.name)
            }
        return resolver@{ ref, publishedName ->
            val id = ref.toIntOrNull()
            id?.let { codes[it] }?.let { return@resolver it }
            val body = publishedName?.trim()?.uppercase()
            if (!body.isNullOrEmpty() && body != NO_LINE) {
                byNameBody[body]?.let { code ->

                    if (id != null) learnedLineAliases[id] = code
                    return@resolver code
                }
            }
            visibleCode(ref).takeIf { it.isNotEmpty() }
        }
    }

    override suspend fun codeForArrival(
        lineRef: String,
        stopId: Int,
        order: Int?,
        sequences: Map<String, List<StopSequence>>,
    ): String {
        val id = lineRef.toIntOrNull() ?: return visibleCode(lineRef)
        visibleCodes()[id]?.let { return it }
        if (order != null && order > 0) {
            val placed =
                sequences.entries
                    .filter { (_, directions) ->
                        directions.count {
                            it.ids.indexOf(stopId) + 1 == order
                        } == 1
                    }.map { it.key }
                    .distinct()
            if (placed.size == 1) return placed.single()
        }
        return lineCodeFor(lineRef)
    }

    override suspend fun visibleCodes(): Map<Int, String> = staticMutex.withLock { lineCodesLocked() }

    override suspend fun internalIdOf(code: String): Int? =
        staticMutex.withLock {
            lineCodesLocked().entries.firstOrNull { it.value == code }?.key
        }

    override suspend fun getStopsForRoute(routeNum: String): List<List<Stop>> =
        staticMutex.withLock {
            routeStopsCache[routeNum]?.let { return it }
            val stops = lineByCodeLocked(routeNum)?.directionStops().orEmpty()
            routeStopsCache[routeNum] = stops
            stops
        }

    override suspend fun stopRefsByDirection(): Map<String, List<Set<String>>> =
        staticMutex.withLock {
            stopRefsByDirectionCache?.let { return it }
            linesLocked()
                .associate { line ->
                    visibleCode(line.name) to
                        line
                            .directionStops()
                            .map { stops -> stops.mapTo(mutableSetOf()) { it.stopRef } }
                }.also { stopRefsByDirectionCache = it }
        }

    override suspend fun getDirectionLabels(routeNum: String): List<String> =
        staticMutex.withLock {
            routeLabelsCache[routeNum]?.let { return it }
            val labels =
                lineByCodeLocked(routeNum)
                    ?.let { line ->
                        listOf(line.stops.asc, line.stops.desc)
                            .filter { it.isNotEmpty() }
                            .mapNotNull { direction ->
                                val from =
                                    direction.firstOrNull()?.name?.takeIf { it.isNotBlank() }
                                        ?: return@mapNotNull null
                                val to =
                                    direction.lastOrNull()?.name?.takeIf { it.isNotBlank() }
                                        ?: return@mapNotNull null
                                if (from == to) from else "$from - $to"
                            }
                    }.orEmpty()
            routeLabelsCache[routeNum] = labels
            labels
        }

    override suspend fun getDirectionOrigins(routeNum: String): List<String> =
        staticMutex.withLock {
            routeOriginsCache[routeNum]?.let { return it }
            val names =
                lineByCodeLocked(routeNum)
                    ?.let {
                        directionNamesFor(it, byTerminal = true)
                    }.orEmpty()
            routeOriginsCache[routeNum] = names
            names
        }

    override suspend fun getDirectionHeadsigns(routeNum: String): List<String> =
        staticMutex.withLock {
            routeHeadsignsCache[routeNum]?.let { return it }
            val headsigns = lineByCodeLocked(routeNum)?.directionHeadsigns().orEmpty()
            routeHeadsignsCache[routeNum] = headsigns
            headsigns
        }

    override suspend fun getRouteShapes(routeNum: String): List<List<GeoPoint>> =
        staticMutex.withLock {
            routeShapesCache[routeNum]?.let { return it }
            val shapes = lineByCodeLocked(routeNum)?.directionShapes().orEmpty()
            routeShapesCache[routeNum] = shapes
            shapes
        }

    override suspend fun getShape(shapeId: String): List<GeoPoint> = emptyList()

    override suspend fun getStopLineLabels(stopId: String): Map<String, String> =
        staticMutex.withLock {
            val id = stopId.toIntOrNull() ?: return@withLock emptyMap()
            stopLabelsCache[stopId]?.let { return@withLock it }
            val lines = linesLocked()
            val labels =
                lines
                    .mapNotNull { line ->
                        val code = visibleCode(line.name)
                        if (code.isEmpty()) return@mapNotNull null
                        val sequences = listOf(line.stops.asc, line.stops.desc).filter { it.isNotEmpty() }
                        val directionIndex =
                            sequences.indexOfFirst { direction -> direction.any { it.id == id } }
                        if (directionIndex < 0) return@mapNotNull null
                        val names = directionNamesFor(line, byTerminal = true)
                        val label =
                            names.getOrNull(directionIndex)
                                ?: sequences.getOrNull(directionIndex)?.lastOrNull()?.name
                        label?.let { code to it }
                    }.toMap()
            stopLabelsCache[stopId] = labels
            labels
        }

    override suspend fun lineColours(): Map<String, String> =
        linesLocked()
            .mapNotNull { line ->
                val colour = line.color ?: return@mapNotNull null
                visibleCode(line.name) to colour
            }.toMap()

    override suspend fun directionNameFor(
        code: String,
        directionIndex: Int,
    ): String? =
        staticMutex.withLock {
            lineByCodeLocked(code)?.let {
                directionNamesFor(it, byTerminal = true).getOrNull(
                    directionIndex,
                )
            }
        }

    private suspend fun lineByCodeLocked(code: String): LineDto? =
        linesLocked().firstOrNull { visibleCode(it.name) == code }

    private fun directionNamesFor(
        line: LineDto,
        byTerminal: Boolean,
    ): List<String> {
        val halves =
            line.name
                .split("-")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .drop(1)
        val directions = listOf(line.stops.asc, line.stops.desc).filter { it.isNotEmpty() }
        if (halves.isEmpty()) return emptyList()
        if (directions.size <= 1) return listOf(halves.joinToString(" - "))

        fun matches(
            half: String,
            stop: String,
        ): Boolean =
            stop.isNotBlank() &&
                (
                    stop.contains(half, ignoreCase = true) ||
                        half.contains(
                            stop,
                            ignoreCase = true,
                        )
                )

        fun placeOf(
            half: String,
            direction: List<LineStopDto>,
        ): Double? {
            val at = direction.indexOfLast { matches(half, it.name) }
            return if (at < 0 || direction.size < 2) null else at.toDouble() / (direction.size - 1)
        }

        fun endsAt(half: String): Int? {
            val atEnd =
                directions.indices.filter {
                    (placeOf(half, directions[it]) ?: -1.0) >= NetworkConstants.END_SHARE
                }
            if (atEnd.size == 1) return atEnd.single()
            val atStart =
                directions.indices.filter {
                    (placeOf(half, directions[it]) ?: 1.0) <= NetworkConstants.START_SHARE
                }
            return if (atStart.size == 1) 1 - atStart.single() else null
        }

        val names = MutableList<String?>(directions.size) { null }
        halves.forEach { half ->
            val at = endsAt(half) ?: return@forEach
            if (names[at] == null) names[at] = half
        }
        directions.forEachIndexed { index, stops ->
            if (names[index] != null) return@forEachIndexed
            val terminal = stops.lastOrNull()?.name.orEmpty()
            val origin = stops.firstOrNull()?.name.orEmpty()
            names[index] =
                when {
                    halves.any { matches(it, terminal) } -> {
                        halves.first { matches(it, terminal) }
                    }

                    halves.any { matches(it, origin) } -> {
                        halves.firstOrNull { !matches(it, origin) }
                    }

                    else -> {
                        null
                    }
                }
        }
        directions.forEachIndexed { index, stops ->
            if (names[index] != null) return@forEachIndexed
            val taken = names.mapNotNull { it }
            names[index] =
                halves.firstOrNull { half ->
                    taken.none {
                        it.equals(half, ignoreCase = true)
                    }
                }
                    ?: halves.getOrNull(index)
        }
        val ends =
            names.mapIndexed { index, name ->
                name ?: directions[index].lastOrNull()?.name.orEmpty()
            }
        if (byTerminal || ends.size != 2) return ends
        return listOf(ends[1], ends[0])
    }
}

private fun lineCodes(lines: List<LineDto>): Map<Int, String> = lines.associate { it.id to visibleCode(it.name) }

private fun List<LineDto>.sequences(): Map<String, List<StopSequence>> {
    val byCode = LinkedHashMap<String, MutableList<StopSequence>>()
    forEach { line ->
        val code = visibleCode(line.name)
        if (code.isEmpty()) return@forEach
        listOf(line.stops.asc, line.stops.desc)
            .filter { it.isNotEmpty() }
            .forEach { direction ->
                byCode
                    .getOrPut(code) { mutableListOf() }
                    .add(
                        StopSequence(
                            direction.map { it.id },
                            direction.lastOrNull()?.name,
                        ),
                    )
            }
    }
    return byCode
}
