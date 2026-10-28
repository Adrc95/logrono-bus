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

package dev.adrc95.logronobus.feature.tracking.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.tracking.TrackingScreen
import dev.adrc95.logronobus.feature.tracking.model.TrackedDepartureDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private val previewStops =
    listOf(
        StopDisplayModel(
            stopRef = "5",
            name = "Artesanos",
            location = LocationDisplayModel(latitude = 42.4600, longitude = -2.4400),
            stopId = "5",
        ),
        StopDisplayModel(
            stopRef = "130",
            name = "La Cadena",
            location = LocationDisplayModel(latitude = 42.4610, longitude = -2.4420),
            stopId = "130",
        ),
    )

@Preview
@Composable
private fun TrackingScreenPreview() {
    LogronoBusTheme {
        TrackingScreen(
            tracking =
                TrackingDisplayModel(
                    departure =
                        TrackedDepartureDisplayModel(
                            displayName = "Varea",
                            timetableId = "B1",
                            lineColor = Color(0xFFE53935),
                        ),
                    timelineStops = previewStops.toImmutableList(),
                    positions =
                        persistentListOf(
                            BusLocationDisplayModel(
                                tripDuid = "7559",
                                vehicleId = "7559",
                                timetableId = "B1",
                                lineColor = Color(0xFFE53935),
                                location = LocationDisplayModel(latitude = 42.4605, longitude = -2.4405),
                            ),
                        ),
                    trackedTripId = "7559",
                    trackedStopRef = "130",
                    riderStopName = "La Cadena",
                    busIndex = 1,
                ),
            nowMs = 0L,
            onBack = {},
            onTripCompleted = {},
            onFollowNext = {},
        )
    }
}
