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

package dev.adrc95.logronobus.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.isCompactWidth
import dev.adrc95.logronobus.core.designsystem.theme.ProvideTopBar
import dev.adrc95.logronobus.core.designsystem.theme.logronoAppBarColors
import dev.adrc95.logronobus.core.ui.component.BusMapView
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_back
import dev.adrc95.logronobus.feature.tracking.component.RouteTimeline
import dev.adrc95.logronobus.feature.tracking.component.TrackingInfoCard
import dev.adrc95.logronobus.feature.tracking.component.TrackingTopBarTitle
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    tracking: TrackingDisplayModel,
    nowMs: Long,
    onBack: NoParamAction,
    onTripCompleted: NoParamAction,
    onFollowNext: NoParamAction,
) {
    val display = tracking
    val trackedDeparture = display.departure
    val tripCompleted = display.completed

    LaunchedEffect(display.completed) {
        if (display.completed) {
            onTripCompleted()
        }
    }

    ProvideTopBar {
        TopAppBar(
            title = {
                TrackingTopBarTitle(
                    routeNum = trackedDeparture?.timetableId,
                    lineColor = trackedDeparture?.lineColor,
                    destination = trackedDeparture?.displayName,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        LogronoIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                    )
                }
            },
            colors = logronoAppBarColors(),
        )
    }
    Box(modifier.fillMaxSize()) {
        val timelineStops = display.timelineStops
        val mapArea: @Composable (Modifier) -> Unit = { m ->
            Box(m) {
                BusMapView(
                    positions = display.positions,
                    stops = timelineStops,
                    trackedTripId = display.trackedTripId.takeUnless { tripCompleted },
                    trackedStopRef = display.trackedStopRef,
                    trackedNextStopRef = display.nextStopRef,
                    onStopClick = null,
                    onBusClick = null,
                    polylines = display.mapPolyline,
                    stopEtas = display.mapEtas,
                    followBus = true,
                    routeColor = trackedDeparture?.lineColor,
                    modifier = Modifier.fillMaxSize(),
                )
                TrackingInfoCard(
                    departure = trackedDeparture,
                    trackedBus = display.trackedBus,
                    stopName = display.riderStopName,
                    delay = display.delay,
                    nowMs = nowMs,
                    completed = tripCompleted,
                    stopState = display.riderStopState,
                    passedAt = display.passedAt,
                    atYourStop = display.atYourStop,
                    nextBus = display.nextBus,
                    nextBusKnown = display.nextBusKnown,
                    onFollowNext = if (display.nextBus != null) onFollowNext else null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                )
            }
        }
        val timeline: @Composable (Modifier) -> Unit = { m ->
            RouteTimeline(
                stops = timelineStops,
                targetStopRef = display.trackedStopRef,
                busIndex = display.busIndex,
                atStopRef = display.atStopRef,
                stopEtas = display.stopEtas,
                lineColor = trackedDeparture?.lineColor,
                completed = tripCompleted,
                modifier = m,
            )
        }

        Box(Modifier.fillMaxSize()) {
            if (isCompactWidth()) {
                Column(Modifier.fillMaxSize()) {
                    mapArea(
                        Modifier
                            .fillMaxWidth()
                            .weight(2f),
                    )
                    if (timelineStops.isNotEmpty()) {
                        HorizontalDivider()
                        timeline(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    }
                }
            } else {
                Row(Modifier.fillMaxSize()) {
                    mapArea(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f),
                    )
                    if (timelineStops.isNotEmpty()) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                        timeline(
                            Modifier
                                .fillMaxHeight()
                                .width(300.dp),
                        )
                    }
                }
            }
        }
    }
}
