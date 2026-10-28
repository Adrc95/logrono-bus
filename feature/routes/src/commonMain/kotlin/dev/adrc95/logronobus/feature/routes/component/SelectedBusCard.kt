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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.component.RouteBadge
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_back
import dev.adrc95.logronobus.core.ui.generated.resources.cd_collapse
import dev.adrc95.logronobus.core.ui.generated.resources.cd_expand
import dev.adrc95.logronobus.core.ui.generated.resources.countdown_min
import dev.adrc95.logronobus.core.ui.generated.resources.due
import dev.adrc95.logronobus.core.ui.generated.resources.not_available
import dev.adrc95.logronobus.core.ui.generated.resources.track_bus
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.feature.routes.model.UpcomingStopDisplayModel
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectedBusCard(
    modifier: Modifier = Modifier,
    bus: BusLocationDisplayModel,
    upcoming: ImmutableList<UpcomingStopDisplayModel>,
    lineColor: Color? = null,
    onDismiss: NoParamAction,
) {
    var expanded by remember(bus.tripDuid) { mutableStateOf(false) }
    val visibleStops = if (expanded) upcoming else upcoming.take(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = logronoCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                bus.timetableId?.let { RouteBadge(route = it, large = false, lineColor = lineColor) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = bus.headsign ?: stringResource(Res.string.track_bus),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    bus.vehicleId?.let {
                        Text(
                            "🚌 #$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (upcoming.size > 1) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) LogronoIcons.ArrowUp else LogronoIcons.ArrowDown,
                            contentDescription =
                                stringResource(
                                    if (expanded) Res.string.cd_collapse else Res.string.cd_expand,
                                ),
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        LogronoIcons.Close,
                        contentDescription = stringResource(Res.string.cd_back),
                    )
                }
            }

            if (upcoming.isEmpty()) {
                Text(
                    stringResource(Res.string.not_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                visibleStops.forEachIndexed { index, stop ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stop.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text =
                                when {
                                    stop.minutes == null -> "–"
                                    stop.minutes <= 0 -> stringResource(Res.string.due)
                                    else -> stringResource(Res.string.countdown_min, stop.minutes)
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
