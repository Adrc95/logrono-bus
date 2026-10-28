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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.component.RouteBadge
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.ui.component.DepartureTimeText
import dev.adrc95.logronobus.core.ui.component.TrackChevron
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_remove_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.last_updated_retrying
import dev.adrc95.logronobus.core.ui.generated.resources.loading_departures
import dev.adrc95.logronobus.core.ui.generated.resources.no_more_departures
import dev.adrc95.logronobus.core.ui.generated.resources.starts_here
import dev.adrc95.logronobus.core.ui.generated.resources.stop_label
import dev.adrc95.logronobus.core.ui.generated.resources.terminates_here
import dev.adrc95.logronobus.core.ui.generated.resources.updated
import dev.adrc95.logronobus.core.ui.generated.resources.updating
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.util.timeAgoLabel
import dev.adrc95.logronobus.feature.favourites.FavouritesConstants
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavouriteStopCard(
    card: FavouriteStopDisplayModel,
    nowMs: Long,
    updatedMs: Long?,
    isRefreshing: Boolean,
    onRemove: NoParamAction,
    onDepartureClick: InputAction<DepartureDisplayModel>,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = logronoCardColors(),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        LogronoIcons.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        card.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        stringResource(Res.string.stop_label, card.stopId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        LogronoIcons.Star,
                        contentDescription = stringResource(Res.string.cd_remove_favourite),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            when {
                card.rows.isEmpty() && !card.hasLoadedDepartures -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = FAVOURITE_DEPARTURES_INDENT, end = 16.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(Res.string.loading_departures),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                card.rows.isEmpty() -> {
                    Text(
                        stringResource(Res.string.no_more_departures),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                    )
                }

                else -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        card.rows.forEach { row ->
                            FavouriteDepartureRow(row, nowMs, onDepartureClick)
                        }
                    }
                }
            }

            if (updatedMs != null) {
                val stale =
                    !isRefreshing && nowMs - updatedMs > FavouritesConstants.STALE_AFTER_MILLIS
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(Modifier.size(10.dp), strokeWidth = 1.5.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        when {
                            isRefreshing -> {
                                stringResource(Res.string.updating)
                            }

                            stale -> {
                                stringResource(
                                    Res.string.last_updated_retrying,
                                    timeAgoLabel(updatedMs, nowMs),
                                )
                            }

                            else -> {
                                stringResource(
                                    Res.string.updated,
                                    timeAgoLabel(updatedMs, nowMs),
                                )
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (stale) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

private val FAVOURITE_DEPARTURES_INDENT = 64.dp

@Composable
private fun FavouriteDepartureRow(
    row: DepartureDisplayModel,
    nowMs: Long,
    onDepartureClick: InputAction<DepartureDisplayModel>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = row.isTrackable) { onDepartureClick(row) }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteBadge(
            route = row.lineCode,
            large = true,
            lineColor = row.lineColor,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text =
                when (val destination = row.destination) {
                    is DepartureDestination.Towards -> {
                        destination.name
                    }

                    DepartureDestination.StartsHere -> {
                        stringResource(Res.string.starts_here)
                    }

                    DepartureDestination.TerminatesHere -> {
                        stringResource(Res.string.terminates_here)
                    }
                },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        DepartureTimeText(
            row.departure,
            nowMs,
            MaterialTheme.typography.titleMedium,
        )
        if (row.isTrackable) {
            Spacer(Modifier.width(4.dp))
            TrackChevron()
        }
    }
}
