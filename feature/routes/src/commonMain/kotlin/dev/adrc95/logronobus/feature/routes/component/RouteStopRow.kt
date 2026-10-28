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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardContainerColor
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardElevatedContainerColor
import dev.adrc95.logronobus.core.ui.component.StopDeparturesSection
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_add_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.cd_collapse
import dev.adrc95.logronobus.core.ui.generated.resources.cd_expand
import dev.adrc95.logronobus.core.ui.generated.resources.cd_remove_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.stop_label
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.feature.routes.model.RouteStopDisplayModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun RouteStopRow(
    modifier: Modifier = Modifier,
    row: RouteStopDisplayModel,
    isLoadingDepartures: Boolean,
    nowMs: Long,
    onStopClick: NoParamAction,
    onToggleFavourite: NoParamAction,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    departuresNotLive: Boolean = false,
    hasPrediction: Boolean = false,
) {
    val stop = row.stop
    Column(
        modifier
            .fillMaxWidth()
            .background(
                if (row.isExpanded) {
                    logronoCardElevatedContainerColor()
                } else {
                    logronoCardContainerColor()
                },
            ).clickable(onClick = onStopClick),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(4.dp),
                    ).padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.stop_label, stop.stopId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stop.name, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onToggleFavourite) {
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
            Icon(
                if (row.isExpanded) LogronoIcons.ArrowUp else LogronoIcons.ArrowDown,
                contentDescription =
                    if (row.isExpanded) {
                        stringResource(Res.string.cd_collapse)
                    } else {
                        stringResource(Res.string.cd_expand)
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (row.isExpanded) {
            StopDeparturesSection(
                rows = row.departures,
                isLoading = isLoadingDepartures,
                nowMs = nowMs,
                onDepartureClick = { departure -> onDepartureClick(departure, stop.stopRef) },
                notLive = departuresNotLive,
                hasPrediction = hasPrediction,
            )
        }
    }
}
