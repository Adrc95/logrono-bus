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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardContainerColor
import dev.adrc95.logronobus.core.ui.component.StopLineRow
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_add_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.cd_remove_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.distance_km
import dev.adrc95.logronobus.core.ui.generated.resources.distance_m
import dev.adrc95.logronobus.core.ui.generated.resources.showing_city_centre
import dev.adrc95.logronobus.core.ui.generated.resources.stops_near_you
import dev.adrc95.logronobus.core.ui.generated.resources.towards
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NearbyList(
    modifier: Modifier = Modifier,
    nearby: NearbyDisplayModel,
    onStopClick: InputAction<StopDisplayModel>,
    onToggleFavourite: InputAction<StopDisplayModel>,
) {
    LazyColumn(modifier.background(logronoCardContainerColor())) {
        item {
            Text(
                if (nearby.isFallback) {
                    stringResource(Res.string.showing_city_centre)
                } else {
                    pluralStringResource(
                        Res.plurals.stops_near_you,
                        nearby.stops.size,
                        nearby.stops.size,
                    )
                },
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (nearby.isFallback) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(
            nearby.stops,
            key = { it.stop.stopRef },
        ) { row ->
            val stop = row.stop
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onStopClick(stop) }
                        .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    LogronoIcons.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stop.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    stop.routeNames.firstOrNull()?.let { name ->
                        Text(
                            name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text =
                            when (val distance = row.distance) {
                                is DistanceDisplayModel.Metres -> {
                                    stringResource(Res.string.distance_m, distance.metres)
                                }

                                is DistanceDisplayModel.Kilometres -> {
                                    stringResource(Res.string.distance_km, distance.kilometres)
                                }
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    StopLineRow(
                        lines = row.lineCodes,
                        routeColors = row.lineColors,
                        routeOrigins = emptyMap(),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                IconButton(onClick = { onToggleFavourite(stop) }) {
                    Icon(
                        if (row.isFavourite) LogronoIcons.Star else LogronoIcons.StarBorder,
                        contentDescription =
                            if (row.isFavourite) {
                                stringResource(Res.string.cd_remove_favourite)
                            } else {
                                stringResource(Res.string.cd_add_favourite)
                            },
                        tint =
                            if (row.isFavourite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
            HorizontalDivider()
        }
    }
}
