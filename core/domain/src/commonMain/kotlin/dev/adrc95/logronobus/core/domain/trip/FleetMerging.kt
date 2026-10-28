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

import dev.adrc95.logronobus.core.common.InputOutputAction
import dev.adrc95.logronobus.core.domain.DomainConstants.SAME_TRIP_TOLERANCE_SECONDS
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FleetActivity
import dev.adrc95.logronobus.core.model.FleetBehind
import dev.adrc95.logronobus.core.model.FleetCall
import dev.adrc95.logronobus.core.model.FleetView
import dev.adrc95.logronobus.core.model.StopSequence
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun callsAt(
    stopId: Int,
    activities: List<FleetActivity>,
    clock: Instant,
    codes: Map<Int, String>,
    direction: Int?,
    windowMinutes: Int,
    sequences: Map<String, List<StopSequence>>,
): List<FleetCall> =
    activities.mapNotNull { activity ->
        val next = activity.nextStopRef?.toIntOrNull() ?: return@mapNotNull null
        if (next != stopId) return@mapNotNull null
        val at = activity.nextStopExpectedAt ?: return@mapNotNull null
        if (at < clock - 60.seconds || at > clock + (windowMinutes * 60).seconds) {
            return@mapNotNull null
        }
        val internal = activity.lineRef.toIntOrNull() ?: return@mapNotNull null
        val code = codes[internal] ?: return@mapNotNull null
        val lineSequences = sequences[code].orEmpty()
        val index = directionOf(activity, lineSequences, stopId) ?: return@mapNotNull null
        if (direction != null && index != direction) return@mapNotNull null
        val stops = lineSequences.getOrNull(index)?.ids.orEmpty()
        FleetCall(
            vehicleId = activity.vehicleRef,
            lineCode = code,
            direction = index,
            at = at,
            delaySeconds = activity.delaySeconds,
            row =
                DepartureTime(
                    displayName = lineSequences.getOrNull(index)?.terminal.orEmpty(),
                    timetableId = code,
                    departTimestamp = at,
                    delaySeconds = activity.delaySeconds,
                    tripId = activity.vehicleRef,
                    vehicleId = activity.vehicleRef,
                    headsign = lineSequences.getOrNull(index)?.terminal,
                    directionIndex = index,
                    terminatesHere = stops.lastOrNull() == stopId,
                    startsHere = stops.firstOrNull() == stopId,
                    predictionInaccurate = activity.predictionInaccurate,
                ),
        )
    }

fun behindAt(
    activities: List<FleetActivity>,
    codes: Map<Int, String>,
    direction: Int?,
    sequences: Map<String, List<StopSequence>>,
): List<FleetBehind> =
    activities.mapNotNull { activity ->
        val next = activity.nextStopRef?.toIntOrNull() ?: return@mapNotNull null
        val at = activity.nextStopExpectedAt ?: return@mapNotNull null
        val internal = activity.lineRef.toIntOrNull() ?: return@mapNotNull null
        val code = codes[internal] ?: return@mapNotNull null
        val index = directionOf(activity, sequences[code].orEmpty(), next) ?: return@mapNotNull null
        if (direction != null && index != direction) return@mapNotNull null
        val nextIndex = sequences[code]?.getOrNull(index)?.ids?.indexOf(next) ?: -1
        if (nextIndex < 0) return@mapNotNull null
        FleetBehind(
            vehicleId = activity.vehicleRef,
            lineCode = code,
            direction = index,
            nextIndex = nextIndex,
            at = at,
            delaySeconds = activity.delaySeconds,
        )
    }

fun withFleet(
    rows: List<DepartureTime>,
    view: FleetView,
    rowTime: InputOutputAction<DepartureTime, Instant?>,
): List<DepartureTime> {
    if (view.calls.isEmpty() && view.behind.isEmpty()) return rows
    val unclaimed = view.calls.toMutableList()
    val behind = view.behind.sortedByDescending { it.nextIndex }.toMutableList()

    fun busFor(row: DepartureTime): FleetBehind? {
        val index = row.directionIndex ?: return null
        val at = rowTime(row) ?: return null
        val sequence = view.sequences[row.timetableId]?.getOrNull(index)?.ids ?: return null
        val stopIndex = sequence.indexOf(view.stopId)
        if (stopIndex < 0) return null
        return behind.firstOrNull { candidate ->
            candidate.lineCode == row.timetableId &&
                candidate.direction == index &&
                candidate.nextIndex <= stopIndex &&
                at >= candidate.at - 60.seconds
        }
    }

    val linked =
        rows.map { row ->
            val at = rowTime(row)
            val call =
                unclaimed
                    .mapNotNull { candidate ->
                        if (candidate.lineCode != row.timetableId) return@mapNotNull null
                        if (row.directionIndex != null && candidate.direction != row.directionIndex) {
                            return@mapNotNull null
                        }
                        val gap = at?.let { candidate.at - it } ?: return@mapNotNull null
                        val apart = if (gap.isNegative()) -gap else gap
                        if (apart > SAME_TRIP_TOLERANCE_SECONDS.seconds) null else candidate to apart
                    }.minByOrNull { it.second }
                    ?.first
            if (call != null) {
                unclaimed.remove(call)
                behind.removeAll { it.vehicleId == call.vehicleId }
                row.copy(
                    tripId = call.vehicleId,
                    vehicleId = call.vehicleId,
                    delaySeconds = call.delaySeconds ?: row.delaySeconds,
                    arrivalStatus = null,
                )
            } else {
                val bus = busFor(row)
                if (bus == null) {
                    row
                } else {
                    behind.remove(bus)
                    row.copy(
                        tripId = bus.vehicleId,
                        vehicleId = bus.vehicleId,
                        delaySeconds = bus.delaySeconds ?: row.delaySeconds,
                        arrivalStatus = null,
                    )
                }
            }
        }
    return linked + unclaimed.map { it.row }
}

private fun directionOf(
    activity: FleetActivity,
    sequences: List<StopSequence>,
    stopId: Int,
): Int? {
    val calls = sequences.indices.filter { stopId in sequences[it].ids }
    return when {
        calls.size == 1 -> calls.single()
        calls.isEmpty() -> null
        activity.directionRef.equals("Vuelta", ignoreCase = true) -> calls.last()
        activity.directionRef.equals("Ida", ignoreCase = true) -> calls.first()
        else -> null
    }
}

fun fleetClock(
    serverTimeMs: Long?,
    now: Instant,
): Instant = serverTimeMs?.let { Instant.fromEpochMilliseconds(it) } ?: now

fun usableActivities(
    activities: List<FleetActivity>,
    servingLines: Set<Int>,
    visibleLines: Set<Int>,
    clock: Instant,
    freshMinutes: Long,
): List<FleetActivity> =
    activities.filter { activity ->
        val internal = activity.lineRef.toIntOrNull() ?: return@filter false
        if (internal !in servingLines) return@filter false
        if (internal !in visibleLines) return@filter false
        if (activity.vehicleRef.isBlank()) return@filter false
        if (activity.nextStopRef?.toIntOrNull() == null) return@filter false
        val recorded = activity.recordedAt ?: return@filter false
        (clock - recorded).inWholeMinutes <= freshMinutes
    }
