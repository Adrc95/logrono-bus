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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.feature.routes.model.ViewMode
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSwitcher(
    modifier: Modifier = Modifier,
    viewMode: ViewMode,
    onSelect: InputAction<ViewMode>,
) {
    val colors =
        SegmentedButtonDefaults.colors(
            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            activeBorderColor = MaterialTheme.colorScheme.primary,
            inactiveBorderColor = Color.Transparent,
        )
    SingleChoiceSegmentedButtonRow(modifier) {
        ViewMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = viewMode == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ViewMode.entries.size),
                colors = colors,
            ) { Text(stringResource(mode.labelRes)) }
        }
    }
}
