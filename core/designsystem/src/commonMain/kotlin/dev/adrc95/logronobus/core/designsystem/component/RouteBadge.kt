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

package dev.adrc95.logronobus.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor

@Composable
fun RouteChip(
    modifier: Modifier = Modifier,
    route: String,
    lineColor: Color? = null,
) {
    Box(
        modifier
            .then(
                if (lineColor == null) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(4.dp),
                    )
                } else {
                    Modifier.background(lineColor, RoundedCornerShape(4.dp))
                },
            ).padding(horizontal = 6.dp, vertical = 1.dp)
            .defaultMinSize(minWidth = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            route,
            style = MaterialTheme.typography.labelSmall,
            color =
                lineColor?.let { it.readableOnColor() }
                    ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun RouteBadge(
    modifier: Modifier = Modifier,
    route: String,
    large: Boolean,
    lineColor: Color? = null,
) {
    val bg =
        lineColor
            ?: if (large) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val fg =
        lineColor?.let { it.readableOnColor() }
            ?: if (large) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier
            .background(bg, RoundedCornerShape(if (large) 6.dp else 4.dp))
            .padding(horizontal = if (large) 8.dp else 5.dp, vertical = if (large) 4.dp else 2.dp)
            .defaultMinSize(minWidth = if (large) 34.dp else 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            route,
            style = if (large) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}
