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

package dev.adrc95.logronobus.feature.tracking.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.time.toClockLabel
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.designsystem.theme.onTimeGreen
import dev.adrc95.logronobus.core.model.RiderStopState
import dev.adrc95.logronobus.core.ui.component.remainingMinutesLabel
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.at_your_stop
import dev.adrc95.logronobus.core.ui.generated.resources.bus_not_reporting
import dev.adrc95.logronobus.core.ui.generated.resources.bus_not_reporting_detail
import dev.adrc95.logronobus.core.ui.generated.resources.follow_next_bus
import dev.adrc95.logronobus.core.ui.generated.resources.live_position
import dev.adrc95.logronobus.core.ui.generated.resources.min_early
import dev.adrc95.logronobus.core.ui.generated.resources.min_late
import dev.adrc95.logronobus.core.ui.generated.resources.next_bus_at
import dev.adrc95.logronobus.core.ui.generated.resources.next_bus_at_delay
import dev.adrc95.logronobus.core.ui.generated.resources.next_bus_loading
import dev.adrc95.logronobus.core.ui.generated.resources.no_more_buses
import dev.adrc95.logronobus.core.ui.generated.resources.on_time
import dev.adrc95.logronobus.core.ui.generated.resources.position_updated
import dev.adrc95.logronobus.core.ui.generated.resources.stop_passed
import dev.adrc95.logronobus.core.ui.generated.resources.stop_passed_at
import dev.adrc95.logronobus.core.ui.generated.resources.trip_completed
import dev.adrc95.logronobus.core.ui.generated.resources.trip_completed_detail
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.util.timeAgoLabel
import dev.adrc95.logronobus.feature.tracking.model.DelayStatus
import dev.adrc95.logronobus.feature.tracking.model.NextBusDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackedDepartureDisplayModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackingInfoCard(
    modifier: Modifier = Modifier,
    departure: TrackedDepartureDisplayModel?,
    trackedBus: BusLocationDisplayModel?,
    stopName: String?,
    delay: DelayStatus,
    nowMs: Long,
    completed: Boolean = false,
    stopState: RiderStopState = RiderStopState.Unknown,
    passedAt: String? = null,
    atYourStop: Boolean = false,
    nextBus: NextBusDisplayModel? = null,
    nextBusKnown: Boolean = false,
    onFollowNext: NoParamAction? = null,
) {
    departure ?: return
    val delayText: @Composable (DelayStatus) -> String? = { status ->
        when (status) {
            DelayStatus.Unknown -> null
            DelayStatus.OnTime -> stringResource(Res.string.on_time)
            is DelayStatus.Late -> stringResource(Res.string.min_late, status.minutes)
            is DelayStatus.Early -> stringResource(Res.string.min_early, status.minutes)
        }
    }
    val statusText = delayText(delay)
    val nextBusLabel = nextBus?.departTimestamp?.toClockLabel()
    val nextBusStatus = nextBus?.let { delayText(it.delay) }
    val statusColor =
        when (delay) {
            is DelayStatus.Late -> MaterialTheme.colorScheme.error
            else -> onTimeGreen()
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = logronoCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                LogronoIcons.Bus,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text =
                        when {
                            completed -> departure.displayName
                            stopState == RiderStopState.Passed -> stringResource(Res.string.stop_passed)
                            else -> stopName ?: departure.displayName
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val headline =
                        when {
                            completed -> {
                                stringResource(Res.string.trip_completed)
                            }

                            stopState == RiderStopState.Passed && passedAt != null -> {
                                stringResource(Res.string.stop_passed_at, passedAt)
                            }

                            stopState == RiderStopState.Passed -> {
                                null
                            }

                            atYourStop -> {
                                stringResource(Res.string.at_your_stop)
                            }

                            else -> {
                                remainingMinutesLabel(departure.departTimestamp, nowMs)
                            }
                        }
                    if (headline != null) {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (!completed && !atYourStop && stopState != RiderStopState.Passed && statusText != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                        )
                    }
                }
                when {
                    completed -> {
                        Text(
                            text = stringResource(Res.string.trip_completed_detail),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    trackedBus == null -> {
                        Column {
                            Text(
                                text = stringResource(Res.string.bus_not_reporting),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(Res.string.bus_not_reporting_detail),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            )
                        }
                    }

                    stopState == RiderStopState.Passed -> {
                        Text(
                            text =
                                when {
                                    nextBusLabel == null && !nextBusKnown -> {
                                        stringResource(Res.string.next_bus_loading)
                                    }

                                    nextBusLabel == null -> {
                                        stringResource(Res.string.no_more_buses)
                                    }

                                    nextBusStatus == null -> {
                                        stringResource(
                                            Res.string.next_bus_at,
                                            nextBusLabel,
                                        )
                                    }

                                    else -> {
                                        stringResource(
                                            Res.string.next_bus_at_delay,
                                            nextBusLabel,
                                            nextBusStatus,
                                        )
                                    }
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> {
                        Text(
                            text =
                                trackedBus.modifiedAtMillis?.let { millis ->
                                    stringResource(Res.string.position_updated, timeAgoLabel(millis, nowMs))
                                } ?: stringResource(Res.string.live_position),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val canFollowNext =
                    onFollowNext != null &&
                        nextBus != null &&
                        (completed || stopState == RiderStopState.Passed)
                if (canFollowNext) {
                    TextButton(
                        onClick = { onFollowNext?.invoke() },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.follow_next_bus),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
