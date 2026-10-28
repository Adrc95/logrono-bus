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

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.tab_buses
import dev.adrc95.logronobus.core.ui.generated.resources.tab_favourites
import dev.adrc95.logronobus.core.ui.generated.resources.tab_nearby
import dev.adrc95.logronobus.core.ui.generated.resources.tab_routes
import dev.adrc95.logronobus.core.ui.generated.resources.tab_scan
import dev.adrc95.logronobus.core.ui.generated.resources.title_all_buses
import dev.adrc95.logronobus.core.ui.generated.resources.title_scan_a_stop
import dev.adrc95.logronobus.feature.buses.api.navigation.BusesNavKey
import dev.adrc95.logronobus.feature.favourites.api.navigation.FavouritesNavKey
import dev.adrc95.logronobus.feature.nearby.api.navigation.NearbyNavKey
import dev.adrc95.logronobus.feature.routes.api.navigation.RoutesNavKey
import dev.adrc95.logronobus.feature.scan.api.navigation.ScanNavKey
import org.jetbrains.compose.resources.StringResource

enum class TopTab(
    val labelRes: StringResource,
    val titleRes: StringResource,
    val icon: ImageVector,
    val key: NavKey,
) {
    FAVOURITES(
        Res.string.tab_favourites,
        Res.string.tab_favourites,
        LogronoIcons.Star,
        FavouritesNavKey,
    ),
    ROUTES(
        Res.string.tab_routes,
        Res.string.tab_routes,
        LogronoIcons.List,
        RoutesNavKey,
    ),
    NEARBY(
        Res.string.tab_nearby,
        Res.string.tab_nearby,
        LogronoIcons.NearMe,
        NearbyNavKey,
    ),
    SCAN(
        Res.string.tab_scan,
        Res.string.title_scan_a_stop,
        LogronoIcons.QrCodeScanner,
        ScanNavKey,
    ),
    BUSES(
        Res.string.tab_buses,
        Res.string.title_all_buses,
        LogronoIcons.Bus,
        BusesNavKey,
    ),
}
