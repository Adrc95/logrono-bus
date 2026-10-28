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

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel

@Composable
internal fun MapHoverCard(
    modifier: Modifier = Modifier,
    bus: BusLocationDisplayModel?,
    stop: StopDisplayModel?,
) {
    if (bus == null && stop == null) return
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small,
                ).padding(8.dp),
    ) {
        Column {
            if (bus != null) {
                val route = bus.timetableId ?: ""
                val title =
                    if (route.isNotEmpty()) {
                        "Route $route: ${bus.headsign}"
                    } else {
                        bus.headsign ?: "Bus"
                    }
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text("Vehicle: ${bus.vehicleId}", style = MaterialTheme.typography.bodySmall)
            } else if (stop != null) {
                Text(stop.name, style = MaterialTheme.typography.labelLarge)
                Text("Stop ID: ${stop.stopRef}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
