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

package dev.adrc95.logronobus.feature.tracking.mapper

import dev.adrc95.logronobus.core.domain.trip.arrivingStopName
import dev.adrc95.logronobus.core.domain.trip.resolveDirectionIndex
import dev.adrc95.logronobus.core.domain.usecase.GetTrackingViewUseCase
import dev.adrc95.logronobus.core.model.TrackingViewInput
import dev.adrc95.logronobus.core.testing.builder.busLocation
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.feature.tracking.mapper.departureDelayStatus
import dev.adrc95.logronobus.feature.tracking.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.DelayStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrackingDisplayModelTest {
    @Test
    fun `given the feed naming a direction when the model is built then the feed wins before the stop is guessed`() {
        val routeStops =
            listOf(
                listOf(stop { withStopRef("a") }, stop { withStopRef("b") }),
                listOf(stop { withStopRef("c") }, stop { withStopRef("b") }),
            )

        assertEquals(
            1,
            resolveDirectionIndex(routeStops, departure { withDirectionIndex(1) }?.directionIndex, "b"),
        )
        assertEquals(0, resolveDirectionIndex(routeStops, departure()?.directionIndex, "a"))
        assertEquals(0, resolveDirectionIndex(routeStops, departure()?.directionIndex, "nowhere"))
    }

    @Test
    fun `given a bus on its way when the model is built then arriving names the stop ahead and stops naming it once past`() {
        val stops =
            listOf(
                stop {
                    withStopRef("a")
                    withLongName("Artesanos")
                    withShortName("Artesanos")
                }.copy(latitude = 42.460, longitude = -2.440),
                stop {
                    withStopRef("b")
                    withLongName("Vitoria")
                    withShortName("Vitoria")
                }.copy(latitude = 42.462, longitude = -2.440),
            )
        val bus =
            busLocation {
                withLatitude(42.461)
                withLongitude(-2.440)
                withTripDuid("t1")
            }

        assertEquals("Vitoria", arrivingStopName(bus, "b", stops))
        assertNull(arrivingStopName(bus, "a", stops))
        assertNull(arrivingStopName(null, "a", stops))
    }

    @Test
    fun `given a confirmed bus when it is followed then it is the one and the direction is resolved`() {
        val routeStops =
            listOf(
                listOf(stop { withStopRef("a") }, stop { withStopRef("b") }),
                listOf(stop { withStopRef("c") }),
            )
        val confirmed =
            busLocation {
                withLatitude(42.4)
                withLongitude(-2.4)
                withTripDuid("t1")
                withVehicleId("V1")
                withTimetableId("4")
            }
        val model =
            TrackingViewInput(
                routeStops = routeStops,
                directionHeadsigns = listOf("b", "c"),
                departure =
                    departure {
                        withTripId("t1")
                        withDirectionIndex(1)
                    },
                tripId = "t1",
                stopRef = "c",
                positions = listOf(confirmed),
            ).display()

        assertEquals(listOf("c"), model.timelineStops.map { it.stopRef })
        assertEquals(confirmed.tripDuid, model.trackedBus?.tripDuid)
        assertEquals(listOf(confirmed.tripDuid), model.positions.map { it.tripDuid })
    }

    @Test
    fun `given a bus reaching the terminus when the model is built then the trip completes and the direction is kept`() {
        val direction =
            listOf(
                stop {
                    withStopRef("a")
                    withLatitude(42.460)
                    withLongitude(-2.44)
                },
                stop {
                    withStopRef("b")
                    withLatitude(42.462)
                    withLongitude(-2.44)
                },
            )
        val other =
            listOf(
                stop {
                    withStopRef("c")
                    withLatitude(42.464)
                    withLongitude(-2.44)
                },
            )

        fun modelWith(latitude: Double) =
            TrackingViewInput(
                routeStops = listOf(direction, other),
                directionHeadsigns = listOf("b", "c"),
                departure =
                    departure {
                        withTripId("trip-1")
                        withDirectionIndex(0)
                    },
                tripId = "trip-1",
                stopRef = "a",
                positions =
                    listOf(
                        busLocation {
                            withLatitude(latitude)
                            withLongitude(-2.44)
                            withTripDuid("trip-1")
                            withVehicleId("trip-1")
                            withTimetableId("4")
                        },
                    ),
            ).display()

        assertFalse(modelWith(42.460).completed, "at the start the trip has not finished")

        val finished = modelWith(42.462)

        assertTrue(finished.completed, "standing at the terminus ends the trip")
        assertEquals(
            direction.map { it.stopRef },
            finished.timelineStops.map { it.stopRef },
            "a finished trip must not change direction",
        )
    }

    @Test
    fun `given a delay when it is read then it is on time only within half a minute`() {
        assertEquals(DelayStatus.Unknown, DelayStatus.from(null as Int?))
        assertEquals(DelayStatus.OnTime, DelayStatus.from(30))
        assertEquals(DelayStatus.Late(3), DelayStatus.from(150))
        assertEquals(DelayStatus.Early(2), DelayStatus.from(-90))
    }

    @Test
    fun `given a timetable prediction when it is read then it claims no delay`() {
        val row =
            departure {
                withDelaySeconds(-780)
                withArrivalStatus("no_report")
            }
        assertEquals(DelayStatus.Unknown, departureDelayStatus(row))
    }
}

private fun TrackingViewInput.display() =
    GetTrackingViewUseCase()(this).toDisplayModel(
        routeColors = emptyMap(),
        trackedShape = RouteLineDisplayModel(),
        routeShapes = emptyList(),
    )
