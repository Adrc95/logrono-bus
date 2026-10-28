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

package dev.adrc95.logronobus.feature.tracking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.rememberNowMs
import dev.adrc95.logronobus.feature.tracking.model.TrackedDepartureDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackingRoute(
    modifier: Modifier = Modifier,
    onBack: NoParamAction,
    viewModel: TrackingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrackingScreen(
        tracking = state.tracking,
        nowMs = rememberNowMs(),
        modifier = modifier,
        onBack = onBack,
        onTripCompleted = viewModel::markTripCompleted,
        onFollowNext = viewModel::followNextBus,
    )
}
