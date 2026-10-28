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

package dev.adrc95.logronobus.feature.routes.api.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.adrc95.logronobus.core.navigation.Navigator
import dev.adrc95.logronobus.feature.routes.RoutesRoute
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import dev.adrc95.logronobus.feature.settings.api.navigation.SettingsNavKey
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import dev.adrc95.logronobus.feature.tracking.api.navigation.TrackingNavKey
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.routesEntry(navigator: Navigator) {
    entry<RoutesNavKey> {
        val viewModel = koinViewModel<RoutesViewModel>()
        RoutesRoute(
            onStopClick = { stop, line, direction ->
                navigator.navigate(StopSheetNavKey(stop.stopRef, line, direction))
            },
            onDepartureClick = { departure, stopRef ->
                viewModel.track(departure.departure, stopRef)
                navigator.navigate(TrackingNavKey)
            },
            viewModel = viewModel,
            onOpenSettings = { navigator.navigate(SettingsNavKey) },
        )
    }
}
