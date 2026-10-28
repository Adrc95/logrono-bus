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

package dev.adrc95.logronobus.feature.routes.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.route_stops_hint
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.routes.model.RouteStopDisplayModel
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun RouteStopsPanel(
    modifier: Modifier = Modifier,
    stops: ImmutableList<RouteStopDisplayModel>,
    directionCount: Int,
    selectedDirection: Int,
    directions: ImmutableList<String>,
    onSelectDirection: InputAction<Int>,
    isLoadingDepartures: Boolean,
    onStopClick: InputAction<String>,
    onToggleFavourite: InputAction<StopDisplayModel>,
    nowMs: Long,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    departuresNotLive: Boolean = false,
    predictedStopRefs: Set<String> = emptySet(),
) {
    Column(modifier) {
        DirectionSwitcher(
            directionCount = directionCount,
            selectedDirection = selectedDirection,
            directionHeadsigns = directions,
            onSelectDirection = onSelectDirection,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    pluralStringResource(Res.plurals.route_stops_hint, stops.size, stops.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            itemsIndexed(stops, key = { _, row -> row.stop.stopRef }) { _, row ->
                RouteStopRow(
                    row = row,
                    isLoadingDepartures = isLoadingDepartures,
                    hasPrediction = row.stop.stopRef in predictedStopRefs,
                    nowMs = nowMs,
                    onStopClick = { onStopClick(row.stop.stopRef) },
                    onToggleFavourite = { onToggleFavourite(row.stop) },
                    onDepartureClick = onDepartureClick,
                    departuresNotLive = departuresNotLive,
                )
                HorizontalDivider()
            }
        }
    }
}
