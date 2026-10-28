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

package dev.adrc95.logronobus.feature.fares.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.InputOutputAction
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardContainerColor
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.no_stops_match
import dev.adrc95.logronobus.feature.fares.model.FareDisplayModel
import dev.adrc95.logronobus.feature.fares.model.fareMunicipalityOptions
import dev.adrc95.logronobus.feature.fares.model.fareTravellerOptions
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FaresList(
    modifier: Modifier = Modifier,
    fares: ImmutableList<FareDisplayModel>,
    visibleFares: List<FareDisplayModel>,
    municipality: String,
    traveller: String,
    isLoading: Boolean,
    onSelectMunicipality: InputAction<String>,
    onSelectTraveller: InputAction<String>,
) {
    Column(modifier = modifier) {
        ChipRow(
            options = fareMunicipalityOptions,
            label = { it.labelRes },
            isSelected = { it.id == municipality },
            onSelect = { onSelectMunicipality(it.id) },
        )
        ChipRow(
            options = fareTravellerOptions,
            label = { it.labelRes },
            isSelected = { it.id == traveller },
            onSelect = { onSelectTraveller(it.id) },
        )
        when {
            isLoading && fares.isEmpty() -> {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(Modifier.size(24.dp)) }
            }

            visibleFares.isEmpty() -> {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(Res.string.no_stops_match),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(Modifier.fillMaxSize().background(logronoCardContainerColor())) {
                    items(visibleFares, key = { it.code }) { fare ->
                        FareRow(fare = fare)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ChipRow(
    modifier: Modifier = Modifier,
    options: List<T>,
    label: InputOutputAction<T, StringResource>,
    isSelected: InputOutputAction<T, Boolean>,
    onSelect: InputAction<T>,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onSelect(option) },
                label = { Text(stringResource(label(option))) },
            )
        }
    }
}
