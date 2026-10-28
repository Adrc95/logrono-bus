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

package dev.adrc95.logronobus.feature.nearby.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.isCompactWidth
import dev.adrc95.logronobus.core.ui.component.BusMapView
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun NearbyMapAndList(
    modifier: Modifier = Modifier,
    nearby: NearbyDisplayModel,
    userLocation: LocationDisplayModel?,
    onStopClick: InputAction<StopDisplayModel>,
    onToggleFavourite: InputAction<StopDisplayModel>,
) {
    val list: @Composable (Modifier) -> Unit = { m ->
        NearbyList(
            nearby = nearby,
            onStopClick = { onStopClick(it) },
            onToggleFavourite = { onToggleFavourite(it) },
            modifier = m,
        )
    }
    val map: @Composable (Modifier) -> Unit = { m ->
        BusMapView(
            positions = persistentListOf(),
            stops = nearby.stops.map { it.stop }.toImmutableList(),
            userLocation = userLocation,
            onStopClick = { onStopClick(it) },
            onBusClick = null,
            modifier = m,
        )
    }
    if (isCompactWidth()) {
        Column(modifier.fillMaxSize()) {
            map(Modifier.fillMaxWidth().weight(1f))
            HorizontalDivider()
            list(Modifier.fillMaxWidth().weight(1f))
        }
    } else {
        Row(modifier.fillMaxSize()) {
            map(Modifier.fillMaxHeight().weight(1f))
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            list(Modifier.fillMaxHeight().width(320.dp))
        }
    }
}
