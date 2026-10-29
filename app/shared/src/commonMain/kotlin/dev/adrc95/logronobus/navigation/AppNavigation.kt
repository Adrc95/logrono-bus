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

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.adrc95.logronobus.feature.buses.api.navigation.BusesNavKey
import dev.adrc95.logronobus.feature.fares.api.navigation.FaresNavKey
import dev.adrc95.logronobus.feature.favourites.api.navigation.FavouritesNavKey
import dev.adrc95.logronobus.feature.nearby.api.navigation.NearbyNavKey
import dev.adrc95.logronobus.feature.routes.api.navigation.RoutesNavKey
import dev.adrc95.logronobus.feature.scan.api.navigation.ScanNavKey
import dev.adrc95.logronobus.feature.settings.api.navigation.SettingsNavKey
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import dev.adrc95.logronobus.feature.tracking.api.navigation.TrackingNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val appNavConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(FavouritesNavKey::class, FavouritesNavKey.serializer())
                    subclass(RoutesNavKey::class, RoutesNavKey.serializer())
                    subclass(NearbyNavKey::class, NearbyNavKey.serializer())
                    subclass(ScanNavKey::class, ScanNavKey.serializer())
                    subclass(BusesNavKey::class, BusesNavKey.serializer())
                    subclass(FaresNavKey::class, FaresNavKey.serializer())
                    subclass(TrackingNavKey::class, TrackingNavKey.serializer())
                    subclass(SettingsNavKey::class, SettingsNavKey.serializer())
                    subclass(StopSheetNavKey::class, StopSheetNavKey.serializer())
                }
            }
    }
