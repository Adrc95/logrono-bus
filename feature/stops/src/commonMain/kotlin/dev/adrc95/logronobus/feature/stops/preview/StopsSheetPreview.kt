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

package dev.adrc95.logronobus.feature.stops.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.feature.stops.StopSheetContent
import dev.adrc95.logronobus.feature.stops.model.StopSheetDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

private val previewRow =
    DepartureDisplayModel(
        lineCode = "B1",
        destination = DepartureDestination.Towards("Varea"),
        departure =
            DepartureTime(
                displayName = "Varea",
                timetableId = "B1",
                departTimestamp = Instant.parse("2026-10-24T06:20:00+02:00"),
            ),
        isTrackable = true,
        vehicleId = "7559",
        lineColor = Color(0xFFE53935),
    )

@Preview
@Composable
private fun StopSheetContentPreview() {
    LogronoBusTheme {
        StopSheetContent(
            display =
                StopSheetDisplayModel(
                    title = "Vitoria",
                    stopId = "88",
                    stopRef = "88",
                    isFavourite = true,
                    rows = persistentListOf(previewRow),
                    isLoading = false,
                    notLive = false,
                ),
            nowMs = 0L,
            onToggleFavourite = {},
            onDepartureClick = {},
        )
    }
}
