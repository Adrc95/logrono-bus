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

package dev.adrc95.logronobus.feature.stops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.ui.component.StopDeparturesSection
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.stop_not_found
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.feature.stops.component.StopSheetHeader
import dev.adrc95.logronobus.feature.stops.model.StopSheetDisplayModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun StopSheetContent(
    modifier: Modifier = Modifier,
    display: StopSheetDisplayModel,
    nowMs: Long,
    onToggleFavourite: NoParamAction,
    onDepartureClick: InputAction<DepartureDisplayModel>,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
    ) {
        StopSheetHeader(
            title = display.title,
            stopId = display.stopId,
            isFavourite = display.isFavourite,
            onToggleFavourite = onToggleFavourite,
        )
        Spacer(Modifier.height(8.dp))
        if (display.missing) {
            Text(
                stringResource(Res.string.stop_not_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
            )
            return@Column
        }
        StopDeparturesSection(
            rows = display.rows,
            isLoading = display.isLoading,
            nowMs = nowMs,
            onDepartureClick = onDepartureClick,
            startPadding = 20.dp,
            notLive = display.notLive,
        )
    }
}
