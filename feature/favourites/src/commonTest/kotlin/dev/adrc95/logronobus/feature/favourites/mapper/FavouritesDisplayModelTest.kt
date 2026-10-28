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

package dev.adrc95.logronobus.feature.favourites.mapper

import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel as departureToDisplayModel

class FavouritesDisplayModelTest {
    @Test
    fun `given a stored favourite when the card is built then it shows the catalogue's name and the rows per line`() {
        val favourites =
            listOf(
                favouriteStop {
                    withStopRef("360")
                    withName("Old English name")
                    withStopId("0")
                },
                favouriteStop {
                    withStopRef("999")
                    withName("Saved only")
                    withStopId("77")
                },
            )
        val cards =
            favourites.toDisplayModels(
                stopsByRef =
                    mapOf(
                        "360" to
                            stop {
                                withStopRef("360")
                                withLongName("Vitoria")
                                withShortName("Vitoria")
                                withStopId("12")
                                withRoutes(listOf("B1", "4"))
                            }.toDisplayModel(),
                    ),
                rows =
                    mapOf(
                        "360" to
                            listOf(
                                departure { withTimetableId("4") }
                                    .departureToDisplayModel(destinationLabel = "Valdegastea"),
                            ),
                    ),
            )

        assertEquals("Vitoria", cards[0].name)
        assertEquals("12", cards[0].stopId)
        assertEquals(
            DepartureDestination.Towards("Valdegastea"),
            cards[0].rows.single().destination,
        )
        assertTrue(cards[0].hasLoadedDepartures)
        assertEquals("Saved only", cards[1].name)
        assertEquals("77", cards[1].stopId)
        assertEquals(emptyList(), cards[1].rows)
    }
}
