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

package dev.adrc95.logronobus.feature.buses.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.buses.AllBusesScreen
import dev.adrc95.logronobus.feature.buses.BusesViewModel
import dev.adrc95.logronobus.feature.buses.model.FleetDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetStatus
import kotlinx.collections.immutable.toImmutableList

private val previewBuses =
    listOf(
        BusLocationDisplayModel(
            tripDuid = "7559",
            vehicleId = "7559",
            timetableId = "B1",
            lineColor = Color(0xFFE53935),
            headsign = "VAREA-LA CAVA",
            location = LocationDisplayModel(latitude = 42.4623, longitude = -2.4441),
        ),
        BusLocationDisplayModel(
            tripDuid = "2751",
            vehicleId = "2751",
            timetableId = "B2",
            lineColor = Color(0xFF1E88E5),
            headsign = "EL ARCO-LA ESTRELLA",
            location = LocationDisplayModel(latitude = 42.4635, longitude = -2.4476),
        ),
    )

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
private fun AllBusesScreenPreview() {
    LogronoBusTheme {
        AllBusesScreen(
            busState =
                BusesViewModel.UiState(
                    fleet =
                        FleetDisplayModel(
                            buses = previewBuses.toImmutableList(),
                            status = FleetStatus.Running,
                            updatedMs = 0L,
                        ),
                    stops = previewStops.toImmutableList(),
                ),
            nowMs = 0L,
            onStopClick = {},
            onOpenFares = {},
        )
    }
}
