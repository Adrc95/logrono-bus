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

package dev.adrc95.logronobus.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable_detail
import dev.adrc95.logronobus.core.ui.generated.resources.loading_departures
import dev.adrc95.logronobus.core.ui.generated.resources.no_departures_yet
import dev.adrc95.logronobus.core.ui.generated.resources.starts_here
import dev.adrc95.logronobus.core.ui.generated.resources.terminates_here
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.util.EMPTY_GRACE_MILLIS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StopDeparturesSection(
    rows: ImmutableList<DepartureDisplayModel>,
    isLoading: Boolean,
    nowMs: Long,
    onDepartureClick: InputAction<DepartureDisplayModel>? = null,
    startPadding: Dp = 44.dp,
    notLive: Boolean = false,
    hasPrediction: Boolean = false,
) {
    var nothingFound by remember(rows, isLoading) { mutableStateOf(false) }
    LaunchedEffect(rows, isLoading) {
        nothingFound = false
        if (rows.isEmpty() && !isLoading) {
            delay(EMPTY_GRACE_MILLIS.milliseconds)
            nothingFound = true
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = 16.dp, bottom = 12.dp),
    ) {
        when {
            isLoading || (rows.isEmpty() && (!nothingFound || hasPrediction)) -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.loading_departures),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            rows.isEmpty() -> {
                if (notLive) {
                    Column {
                        Text(
                            stringResource(Res.string.live_feed_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            stringResource(Res.string.live_feed_unavailable_detail),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        stringResource(Res.string.no_departures_yet),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                rows.forEach { row ->
                    val dep = row.departure
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = onDepartureClick != null && row.isTrackable) {
                                onDepartureClick?.invoke(row)
                            }.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val lineColor = row.lineColor
                        Box(
                            Modifier
                                .background(
                                    color =
                                        lineColor
                                            ?: MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp),
                                ).padding(horizontal = 5.dp, vertical = 2.dp)
                                .defaultMinSize(minWidth = 26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                row.lineCode,
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    lineColor?.let { it.readableOnColor() }
                                        ?: MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                when (val destination = row.destination) {
                                    is DepartureDestination.Towards -> destination.name
                                    DepartureDestination.StartsHere -> stringResource(Res.string.starts_here)
                                    DepartureDestination.TerminatesHere -> stringResource(Res.string.terminates_here)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            row.vehicleId?.let { vehicle ->
                                Text(
                                    "🚌 #$vehicle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }

                        DepartureTimeText(dep, nowMs, MaterialTheme.typography.labelMedium)
                        if (onDepartureClick != null && row.isTrackable) {
                            Spacer(Modifier.width(4.dp))
                            TrackChevron()
                        }
                    }
                }
            }
        }
    }
}
