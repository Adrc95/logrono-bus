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

package dev.adrc95.logronobus.feature.nearby.mapper

import dev.adrc95.logronobus.core.testing.builder.nearbyStop
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.feature.nearby.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NearbyDisplayModelTest {
    @Test
    fun `given distances under and over a kilometre when they are shown then the short ones read in metres and the rest in kilometres`() {
        fun distance(meters: Double) =
            listOf(
                nearbyStop {
                    withStop(stop { withStopRef("1") })
                    withDistanceMeters(meters)
                },
            ).toDisplayModel(favouriteRefs = emptySet(), isFallback = false).stops.single().distance

        assertEquals(DistanceDisplayModel.Metres(245), distance(245.4))
        assertEquals(DistanceDisplayModel.Metres(999), distance(999.0))
        assertEquals(DistanceDisplayModel.Kilometres(1.2), distance(1234.0))
        assertEquals(DistanceDisplayModel.Kilometres(2.0), distance(1999.0))
    }

    @Test
    fun `given nearby stops when the list is built then the walking order is kept and the lines read as a rider reads them`() {
        val nearby =
            listOf(
                nearbyStop {
                    withStop(
                        stop {
                            withStopRef("1")
                            withRoutes(listOf("B1", "10", "4"))
                        },
                    )
                    withDistanceMeters(90.0)
                },
                nearbyStop {
                    withStop(
                        stop {
                            withStopRef("2")
                            withRoutes(listOf("4"))
                        },
                    )
                    withDistanceMeters(2500.0)
                },
            ).toDisplayModel(favouriteRefs = setOf("2"), isFallback = false)

        assertEquals(listOf("1", "2"), nearby.stops.map { it.stop.stopRef })
        assertEquals(listOf("4", "10", "B1"), nearby.stops.first().lineCodes)
        assertEquals(listOf(false, true), nearby.stops.map { it.isFavourite })
        assertEquals(DistanceDisplayModel.Kilometres(2.5), nearby.stops[1].distance)
        assertEquals(false, nearby.isFallback)
    }
}
