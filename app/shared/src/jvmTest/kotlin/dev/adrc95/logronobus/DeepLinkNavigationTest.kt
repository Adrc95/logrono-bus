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

package dev.adrc95.logronobus

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.adrc95.logronobus.core.navigation.NavigationState
import dev.adrc95.logronobus.core.navigation.Navigator
import dev.adrc95.logronobus.core.navigation.deeplink.DeepLinkRegistry
import dev.adrc95.logronobus.feature.buses.api.navigation.BusesNavKey
import dev.adrc95.logronobus.feature.fares.api.navigation.FaresNavKey
import dev.adrc95.logronobus.feature.favourites.api.navigation.FavouritesNavKey
import dev.adrc95.logronobus.feature.nearby.api.navigation.NearbyNavKey
import dev.adrc95.logronobus.feature.routes.api.navigation.RoutesNavKey
import dev.adrc95.logronobus.feature.scan.api.navigation.ScanNavKey
import dev.adrc95.logronobus.feature.settings.api.navigation.SettingsNavKey
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import dev.adrc95.logronobus.feature.tracking.api.navigation.TrackingNavKey
import dev.adrc95.logronobus.navigation.logronoBusEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkNavigationTest {
    private fun deepLinks(): DeepLinkRegistry {
        val navigator = Navigator(NavigationState(RoutesNavKey, NavBackStack<NavKey>(RoutesNavKey)))
        return DeepLinkRegistry().also { deepLinks -> logronoBusEntries(navigator, deepLinks) {} }
    }

    @Test
    fun `given any screen link when it is opened then it lands on its screen`() {
        val links =
            mapOf(
                "logronobus://favourites" to FavouritesNavKey,
                "logronobus://nearby" to NearbyNavKey,
                "logronobus://scan" to ScanNavKey,
                "logronobus://buses" to BusesNavKey,
                "logronobus://fares" to FaresNavKey,
                "logronobus://routes" to RoutesNavKey,
                "logronobus://tracking" to TrackingNavKey,
                "logronobus://settings" to SettingsNavKey,
            )

        links.forEach { (uri, key) -> assertEquals(key, deepLinks().resolve(uri)?.key, uri) }
    }

    @Test
    fun `given a stop link with filters when it is opened then the sheet opens on that stop`() {
        assertEquals(
            StopSheetNavKey(stopRef = "123", lineFilter = "6", directionFilter = 1),
            deepLinks().resolve("logronobus://stops/123?line=6&direction=1")?.key,
        )
    }

    @Test
    fun `given a stop link without filters when it is opened then the sheet opens on that stop`() {
        assertEquals(StopSheetNavKey(stopRef = "123"), deepLinks().resolve("logronobus://stops/123")?.key)
    }

    @Test
    fun `given a link nobody registered when it is opened then there is no destination`() {
        assertNull(deepLinks().resolve("logronobus://unknown"))
    }
}
