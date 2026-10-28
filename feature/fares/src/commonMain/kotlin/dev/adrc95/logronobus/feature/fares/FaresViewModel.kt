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

import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.domain.usecase.GetFareProductsUseCase
import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.feature.fares.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.fares.mapper.toDisplayModels
import dev.adrc95.logronobus.feature.fares.model.FareDisplayModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FaresViewModel(
    private val getFareProducts: GetFareProductsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    private val fares: StateFlow<List<Fare>?> =
        flow {
            emit(null)
            emit(safeCall(emptyList()) { getFareProducts() })
        }.stateInViewModel(this, null)

    val uiState: StateFlow<UiState> =
        combine(fares, _uiState) { domain, state ->
            state.copy(
                fares = domain.orEmpty().map { fare -> fare.toDisplayModel() }.toImmutableList(),
                visibleFares =
                    domain
                        .orEmpty()
                        .toDisplayModels(
                            municipality = FareMunicipality.fromId(state.municipalityId),
                            traveller = FareTraveller.fromId(state.travellerId),
                        ).toImmutableList(),
                isLoading = domain == null,
            )
        }.stateInViewModel(this, UiState())

    fun selectFareMunicipality(id: String) {
        _uiState.update { it.copy(municipalityId = id) }
    }

    fun selectFareTraveller(id: String) {
        _uiState.update { it.copy(travellerId = id) }
    }

    data class UiState(
        val fares: ImmutableList<FareDisplayModel> = persistentListOf(),
        val visibleFares: ImmutableList<FareDisplayModel> = persistentListOf(),
        val isLoading: Boolean = false,
        val municipalityId: String = FareMunicipality.LOGRONO.name,
        val travellerId: String = FareTraveller.ALL.name,
    )
}
