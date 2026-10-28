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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.onTimeGreen
import dev.adrc95.logronobus.core.domain.departure.hasMeasuredDelay
import dev.adrc95.logronobus.core.domain.departure.isLive
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.live_time
import org.jetbrains.compose.resources.stringResource

@Composable
private fun departureCountdownColor(departure: DepartureTime): Color {
    if (!departure.hasMeasuredDelay) return MaterialTheme.colorScheme.onSurface
    val delaySeconds = departure.delaySeconds ?: return MaterialTheme.colorScheme.onSurface
    return when {
        delaySeconds > 30 -> MaterialTheme.colorScheme.error
        delaySeconds < -30 -> onTimeGreen()
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
fun DepartureTimeText(
    departure: DepartureTime,
    nowMs: Long,
    style: TextStyle,
) {
    val color = departureCountdownColor(departure)
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (departure.isLive) {
            Icon(
                LogronoIcons.RssFeed,
                contentDescription = stringResource(Res.string.live_time),
                tint = color,
                modifier =
                    Modifier.size(
                        with(LocalDensity.current) {
                            style.fontSize.toDp() * 0.8f
                        },
                    ),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(departure.departureLabel(nowMs), style = style, color = color)
    }
}
