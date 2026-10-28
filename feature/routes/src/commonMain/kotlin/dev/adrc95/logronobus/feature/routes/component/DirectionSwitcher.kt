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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.direction_numbered
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun DirectionSwitcher(
    modifier: Modifier = Modifier,
    directionCount: Int,
    selectedDirection: Int,
    directionHeadsigns: ImmutableList<String>,
    onSelectDirection: InputAction<Int>,
    floating: Boolean = false,
) {
    if (directionCount <= 1) return
    val labels =
        if (directionHeadsigns.size >= directionCount) {
            directionHeadsigns.take(directionCount)
        } else {
            (0 until directionCount).map { stringResource(Res.string.direction_numbered, it + 1) }
        }
    val segmentedRow = @Composable { rowModifier: Modifier ->
        val contentPadding =
            SegmentedButtonDefaults.ContentPadding.let { defaults ->
                PaddingValues(
                    start = 20.dp,
                    top = defaults.calculateTopPadding(),
                    end = 12.dp,
                    bottom = defaults.calculateBottomPadding(),
                )
            }
        SingleChoiceSegmentedButtonRow(rowModifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            labels.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedDirection == index,
                    onClick = { onSelectDirection(index) },
                    shape = SegmentedButtonDefaults.itemShape(index, labels.size),
                    contentPadding = contentPadding,
                ) {
                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
    if (floating) {
        Card(
            modifier = modifier,
            colors = logronoCardColors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) { segmentedRow(Modifier) }
    } else {
        Column(modifier) {
            segmentedRow(Modifier.fillMaxWidth())
            HorizontalDivider()
        }
    }
}
