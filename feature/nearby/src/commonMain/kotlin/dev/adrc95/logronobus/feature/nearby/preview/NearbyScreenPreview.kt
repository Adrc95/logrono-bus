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

package dev.adrc95.logronobus.feature.nearby.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.nearby.NearbyScreen
import dev.adrc95.logronobus.feature.nearby.NearbyViewModel
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyState
import dev.adrc95.logronobus.feature.nearby.model.NearbyStopDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private val previewStops =
    listOf(
        NearbyStopDisplayModel(
            stop =
                StopDisplayModel(
                    stopRef = "88",
                    name = "Vitoria",
                    location = LocationDisplayModel(latitude = 42.4643, longitude = -2.4455),
                    stopId = "88",
                ),
            distance = DistanceDisplayModel.Metres(120),
            walkMinutes = 2,
            lineCodes = persistentListOf("B1"),
            lineColors = mapOf("B1" to Color(0xFFE53935)),
            lineOrigins = mapOf("B1" to "Varea"),
            isFavourite = true,
        ),
        NearbyStopDisplayModel(
            stop =
                StopDisplayModel(
                    stopRef = "5",
                    name = "Artesanos",
                    location = LocationDisplayModel(latitude = 42.4600, longitude = -2.4400),
                    stopId = "5",
                ),
            distance = DistanceDisplayModel.Kilometres(1.4),
            walkMinutes = 17,
            lineCodes = persistentListOf("2", "10"),
            lineColors = mapOf("2" to Color(0xFFFDD835), "10" to Color(0xFF00ACC1)),
            lineOrigins = mapOf("2" to "Varea", "10" to "El Arco"),
            isFavourite = false,
        ),
    )

@Preview
@Composable
private fun NearbyScreenPreview() {
    LogronoBusTheme {
        NearbyScreen(
            nearbyState =
                NearbyViewModel.UiState(
                    nearbyState =
                        NearbyState.Located(
                            LocationDisplayModel(latitude = 42.4623, longitude = -2.4441),
                        ),
                    nearby = NearbyDisplayModel(stops = previewStops.toImmutableList(), isFallback = false),
                    favouriteRefs = setOf("88"),
                ),
            onReload = {},
            onStopClick = {},
            onToggleFavourite = {},
        )
    }
}

@Preview
@Composable
private fun NearbyScreenPermissionDeniedPreview() {
    LogronoBusTheme {
        NearbyScreen(
            nearbyState = NearbyViewModel.UiState(nearbyState = NearbyState.PermissionDenied),
            onReload = {},
            onStopClick = {},
            onToggleFavourite = {},
        )
    }
}
