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

package dev.adrc95.logronobus.feature.favourites.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.saved_stops
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun FavouritesList(
    modifier: Modifier = Modifier,
    cards: ImmutableList<FavouriteStopDisplayModel>,
    savedCount: Int,
    nowMs: Long,
    updatedMs: Long?,
    isRefreshing: Boolean,
    onRefresh: NoParamAction,
    onRemove: InputAction<String>,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxWidth(),
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    pluralStringResource(
                        Res.plurals.saved_stops,
                        savedCount,
                        savedCount,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            items(cards, key = { it.stopRef }) { card ->
                FavouriteStopCard(
                    card = card,
                    nowMs = nowMs,
                    updatedMs = updatedMs,
                    isRefreshing = isRefreshing,
                    onRemove = { onRemove(card.stopRef) },
                    onDepartureClick = { row ->
                        onDepartureClick(
                            row,
                            card.stopRef,
                        )
                    },
                )
            }
        }
    }
}
