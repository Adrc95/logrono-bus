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

package dev.adrc95.logronobus.feature.routes.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.feature.routes.component.RoutesHeader
import dev.adrc95.logronobus.feature.routes.model.ViewMode

@Preview
@Composable
private fun RoutesHeaderPreview() {
    LogronoBusTheme {
        RoutesHeader(
            compact = false,
            selectedRouteNum = null,
            viewMode = ViewMode.MAP,
            onBack = {},
            onSelectViewMode = {},
            onOpenSettings = {},
        )
    }
}

@Preview
@Composable
private fun RoutesHeaderRoutePreview() {
    LogronoBusTheme {
        RoutesHeader(
            compact = true,
            selectedRouteNum = "B1",
            viewMode = ViewMode.MAP,
            onBack = {},
            onSelectViewMode = {},
            onOpenSettings = {},
        )
    }
}
