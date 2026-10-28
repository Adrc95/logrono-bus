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

package dev.adrc95.logronobus.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.navigation.Navigator
import dev.adrc95.logronobus.feature.buses.api.navigation.busesEntry
import dev.adrc95.logronobus.feature.fares.api.navigation.faresEntry
import dev.adrc95.logronobus.feature.favourites.api.navigation.favouritesEntry
import dev.adrc95.logronobus.feature.nearby.api.navigation.nearbyEntry
import dev.adrc95.logronobus.feature.routes.api.navigation.routesEntry
import dev.adrc95.logronobus.feature.scan.api.navigation.scanEntry
import dev.adrc95.logronobus.feature.settings.api.navigation.settingsEntry
import dev.adrc95.logronobus.feature.stops.api.navigation.stopsSheetEntry
import dev.adrc95.logronobus.feature.tracking.api.navigation.trackingEntry

fun logronoBusEntries(
    navigator: Navigator,
    onShowMessage: InputAction<String>,
): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        favouritesEntry(
            navigator = navigator,
            onShowMessage = onShowMessage,
        )
        nearbyEntry(navigator = navigator)
        scanEntry(navigator = navigator)
        busesEntry(navigator = navigator)
        faresEntry(navigator = navigator)
        routesEntry(navigator = navigator)
        stopsSheetEntry(navigator = navigator)
        trackingEntry(navigator = navigator)
        settingsEntry(navigator = navigator)
    }
