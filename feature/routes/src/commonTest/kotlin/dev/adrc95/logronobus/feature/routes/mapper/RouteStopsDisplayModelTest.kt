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

package dev.adrc95.logronobus.feature.routes.mapper

import dev.adrc95.logronobus.core.domain.trip.stopRoles
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.feature.routes.mapper.toRouteStopsDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteStopsDisplayModelTest {
    @Test
    fun `given a direction with an open stop when its rows are built then only the open stop carries them`() {
        val stops =
            listOf(
                stop { withStopRef("a") },
                stop { withStopRef("b") },
                stop { withStopRef("c") },
            )
        val rows =
            stops.toRouteStopsDisplayModel(
                selectedStopRef = "b",
                favouriteRefs = setOf("c"),
                departures = listOf(departure()),
                lineLabel = "Madre de Dios",
                roles = stops.stopRoles(),
            )

        assertEquals(listOf(false, true, false), rows.map { it.isExpanded })
        assertEquals(listOf(0, 1, 0), rows.map { it.departures.size })
        assertEquals(listOf(false, false, true), rows.map { it.isFavourite })
        assertEquals(
            DepartureDestination.Towards("Madre de Dios"),
            rows[1].departures.single().destination,
        )
    }
}
