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

package dev.adrc95.logronobus.feature.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.TripleInputAction
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import dev.adrc95.logronobus.feature.routes.component.RoutesHeader
import dev.adrc95.logronobus.feature.routes.model.ViewMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RoutesRoute(
    modifier: Modifier = Modifier,
    viewModel: RoutesViewModel = koinViewModel(),
    onStopClick: TripleInputAction<StopDisplayModel, String?, Int?>,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    onOpenSettings: NoParamAction,
) {
    RoutesScreen(
        viewModel = viewModel,
        onStopClick = onStopClick,
        onDepartureClick = onDepartureClick,
        onOpenSettings = onOpenSettings,
    )
}
