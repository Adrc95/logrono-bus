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

package dev.adrc95.logronobus.feature.stops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.util.rememberNowMs
import dev.adrc95.logronobus.feature.stops.model.StopSheetDisplayModel

@Composable
fun StopsSheetRoute(
    modifier: Modifier = Modifier,
    onDepartureClick: InputAction<DepartureDisplayModel>,
    viewModel: StopsSheetViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheet = uiState ?: return
    StopSheetContent(
        modifier = modifier,
        display = sheet,
        nowMs = rememberNowMs(),
        onToggleFavourite = viewModel::toggleFavourite,
        onDepartureClick = { row -> onDepartureClick(row) },
    )
}
