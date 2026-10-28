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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pushpal.jetlime.EventPointType
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.at_stop
import dev.adrc95.logronobus.core.ui.generated.resources.next_stop
import dev.adrc95.logronobus.core.ui.generated.resources.your_stop
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeApi::class)
@Composable
fun RouteTimeline(
    modifier: Modifier = Modifier,
    stops: ImmutableList<StopDisplayModel>,
    targetStopRef: String?,
    busIndex: Int,
    stopEtas: Map<String, StopEtaDisplayModel>,
    completed: Boolean = false,
    atStopRef: String? = null,
    lineColor: Color? = null,
) {
    val busHere = if (completed) stops.size else busIndex
    val atStopIndex =
        remember(stops, atStopRef) {
            atStopRef?.let { ref -> stops.indexOfFirst { it.stopRef == ref } } ?: -1
        }
    val currentIndex =
        remember(busHere, atStopIndex, stops.size, completed) {
            when {
                stops.isEmpty() -> -1
                completed -> stops.size
                busHere < 0 && atStopIndex < 0 -> -1
                atStopIndex >= 0 -> atStopIndex
                else -> busHere.coerceAtMost(stops.lastIndex)
            }
        }
    var rememberedCurrent by remember(stops) { mutableIntStateOf(-1) }
    if (currentIndex >= 0) rememberedCurrent = currentIndex
    val shownIndex = if (currentIndex >= 0) currentIndex else rememberedCurrent
    val nextIndex =
        remember(shownIndex, stops.size) {
            if (shownIndex < 0 || shownIndex >= stops.size) -1 else (shownIndex + 1).coerceAtMost(stops.lastIndex)
        }
    val busIsStanding = atStopIndex >= 0
    val targetIndex =
        remember(stops, targetStopRef) {
            stops.indexOfFirst { it.stopRef == targetStopRef }
        }

    val checkPainter = rememberVectorPainter(LogronoIcons.Check)
    val targetPainter = rememberVectorPainter(LogronoIcons.Place)
    val primary = lineColor ?: MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val passedColor = MaterialTheme.colorScheme.secondary

    val listState = rememberLazyListState()

    val nextStepBlink by rememberInfiniteTransition(label = "next-stop-step").animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(550, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "next-stop-step-alpha",
    )

    LaunchedEffect(shownIndex, targetIndex, stops.size, completed) {
        val focus =
            when {
                completed -> stops.lastIndex
                shownIndex in stops.indices -> shownIndex
                else -> targetIndex
            }
        if (focus < 0) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.viewportSize.height }.first { it > 0 }
        val viewport = listState.layoutInfo.viewportSize.height
        val itemHeight =
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull()
                ?.size ?: 0
        listState.animateScrollToItem(focus, -(viewport - itemHeight) / 2)
    }

    JetLimeColumn(
        itemsList = ItemsList(stops),
        listState = listState,
        modifier = modifier.padding(start = 4.dp, top = 12.dp, end = 12.dp),
        key = { _, stop -> stop.stopRef },
        style = JetLimeDefaults.columnStyle(lineBrush = JetLimeDefaults.lineSolidBrush(primary)),
    ) { index, stop, position ->
        val isAtStop = atStopRef != null && stop.stopRef == atStopRef
        val hasPassed = shownIndex >= 0 && index < shownIndex
        val isCurrent = shownIndex >= 0 && index == shownIndex
        val isNext = busIsStanding && nextIndex >= 0 && index == nextIndex
        val isTarget = stop.stopRef == targetStopRef

        val etaText =
            stopEtas[stop.stopRef]?.let { eta ->
                if (eta.upcoming || isCurrent) eta.label else null
            }

        val stepAlpha = if (isTarget && isCurrent) nextStepBlink else 1f

        JetLimeExtendedEvent(
            style =
                JetLimeEventDefaults.eventStyle(
                    position = position,
                    pointType =
                        when {
                            isTarget -> {
                                EventPointType.custom(
                                    icon = targetPainter,
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = stepAlpha),
                                )
                            }

                            hasPassed -> {
                                EventPointType.custom(
                                    icon = checkPainter,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            isCurrent -> {
                                EventPointType.filled()
                            }

                            else -> {
                                EventPointType.EMPTY
                            }
                        },
                    pointFillColor =
                        when {
                            isTarget -> primary.copy(alpha = stepAlpha)
                            isCurrent -> primary
                            hasPassed -> passedColor.copy(alpha = 0.45f)
                            isNext -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                    pointColor =
                        when {
                            isTarget -> primary.copy(alpha = stepAlpha)
                            isCurrent -> primary
                            hasPassed -> passedColor.copy(alpha = 0.45f)
                            isNext -> primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                    pointStrokeColor =
                        when {
                            isTarget -> primary.copy(alpha = stepAlpha)
                            isCurrent -> primary
                            hasPassed -> passedColor.copy(alpha = 0.45f)
                            isNext -> primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                    pointAnimation = if (isCurrent && !isTarget) JetLimeEventDefaults.pointAnimation() else null,
                ),
            additionalContentMaxWidth = 52.dp,
            additionalContent = {
                Box(
                    Modifier.width(52.dp).padding(end = 8.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (!hasPassed && etaText != null) {
                        Text(
                            etaText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTarget || isCurrent) primary else onSurfaceVariant,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            },
        ) {
            Column(Modifier.padding(bottom = 4.dp)) {
                Text(
                    stop.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasPassed) onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isTarget || isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val label =
                    when {
                        isTarget -> stringResource(Res.string.your_stop)
                        isAtStop -> stringResource(Res.string.at_stop)
                        isCurrent -> stringResource(Res.string.next_stop)
                        isNext && busIsStanding -> stringResource(Res.string.next_stop)
                        else -> null
                    }
                label?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = primary,
                    )
                }
                Text(
                    stop.stopId,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                )
            }
        }
    }
}
