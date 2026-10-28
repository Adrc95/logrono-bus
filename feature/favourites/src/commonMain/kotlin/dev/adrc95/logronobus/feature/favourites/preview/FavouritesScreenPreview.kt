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

package dev.adrc95.logronobus.feature.favourites.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.feature.favourites.FavouritesScreen
import dev.adrc95.logronobus.feature.favourites.FavouritesViewModel
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

private fun previewRow(
    code: String,
    destination: String,
    time: Instant,
    color: Color,
) = DepartureDisplayModel(
    lineCode = code,
    destination = DepartureDestination.Towards(destination),
    departure =
        DepartureTime(
            displayName = destination,
            timetableId = code,
            departTimestamp = time,
        ),
    isTrackable = true,
    vehicleId = "7559",
    lineColor = color,
)

@Preview
@Composable
private fun FavouritesScreenPreview() {
    LogronoBusTheme {
        FavouritesScreen(
            favouritesState =
                FavouritesViewModel.UiState(
                    cards =
                        persistentListOf(
                            FavouriteStopDisplayModel(
                                stopRef = "157",
                                name = "Vitoria",
                                stopId = "157",
                                rows =
                                    persistentListOf(
                                        previewRow(
                                            "B1",
                                            "Varea",
                                            Instant.parse("2026-10-24T06:20:00+02:00"),
                                            Color(0xFFE53935),
                                        ),
                                        previewRow(
                                            "10",
                                            "El Arco",
                                            Instant.parse("2026-10-24T07:22:00+02:00"),
                                            Color(0xFF00ACC1),
                                        ),
                                    ),
                                hasLoadedDepartures = true,
                            ),
                        ),
                    isLoading = false,
                    updatedMs = 0L,
                ),
            nowMs = 0L,
            onBrowseRoutes = {},
            onDepartureClick = { _, _ -> },
            onShowMessage = {},
            onStopClick = {},
            onToggleFavourite = {},
            onRefresh = {},
            onRemove = {},
            onSearch = {},
        )
    }
}
