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

package dev.adrc95.logronobus.feature.fares

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.ProvideTopBar
import dev.adrc95.logronobus.core.designsystem.theme.logronoAppBarColors
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_back
import dev.adrc95.logronobus.core.ui.generated.resources.title_fares
import dev.adrc95.logronobus.feature.fares.component.FaresList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FaresRoute(
    modifier: Modifier = Modifier,
    onBack: NoParamAction,
    viewModel: FaresViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FaresScreen(
        state = state,
        onSelectMunicipality = viewModel::selectFareMunicipality,
        onSelectTraveller = viewModel::selectFareTraveller,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun FaresScreen(
    modifier: Modifier = Modifier,
    state: FaresViewModel.UiState,
    onSelectMunicipality: InputAction<String>,
    onSelectTraveller: InputAction<String>,
    onBack: NoParamAction,
) {
    ProvideTopBar {
        TopAppBar(
            title = { Text(stringResource(Res.string.title_fares)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        LogronoIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                    )
                }
            },
            colors = logronoAppBarColors(),
        )
    }
    FaresList(
        modifier = modifier.fillMaxSize(),
        fares = state.fares,
        visibleFares = state.visibleFares,
        municipality = state.municipalityId,
        traveller = state.travellerId,
        isLoading = state.isLoading,
        onSelectMunicipality = onSelectMunicipality,
        onSelectTraveller = onSelectTraveller,
    )
}
