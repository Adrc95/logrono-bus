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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.track_bus
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackingTopBarTitle(
    modifier: Modifier = Modifier,
    routeNum: String?,
    lineColor: Color?,
    destination: String?,
) {
    val barContentColor = LocalContentColor.current
    Column(
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.track_bus))
            if (routeNum != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .background(
                            color = lineColor ?: barContentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                        ).padding(horizontal = 6.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = routeNum,
                        style = MaterialTheme.typography.labelMedium,
                        color = lineColor?.readableOnColor() ?: barContentColor,
                    )
                }
            }
        }
        if (destination != null) {
            Text(
                text = destination,
                style = MaterialTheme.typography.bodySmall,
                color = barContentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
