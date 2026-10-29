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

package dev.adrc95.logronobus.di

import dev.adrc95.logronobus.core.data.di.DataModule
import dev.adrc95.logronobus.core.database.di.DatabaseModule
import dev.adrc95.logronobus.core.device.di.DeviceModule
import dev.adrc95.logronobus.core.domain.di.DomainModule
import dev.adrc95.logronobus.core.network.di.NetworkModule
import dev.adrc95.logronobus.core.preference.di.PreferenceModule
import dev.adrc95.logronobus.feature.buses.di.BusesModule
import dev.adrc95.logronobus.feature.fares.di.FaresModule
import dev.adrc95.logronobus.feature.favourites.di.FavouritesModule
import dev.adrc95.logronobus.feature.nearby.di.NearbyModule
import dev.adrc95.logronobus.feature.routes.di.RoutesModule
import dev.adrc95.logronobus.feature.scan.di.ScanModule
import dev.adrc95.logronobus.feature.settings.di.SettingsModule
import dev.adrc95.logronobus.feature.stops.di.StopsModule
import dev.adrc95.logronobus.feature.tracking.di.TrackingModule
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

@Module
@ComponentScan("dev.adrc95")
class AppModule

@KoinApplication(
    modules = [
        AppModule::class,
        DataModule::class,
        DeviceModule::class,
        DomainModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        PreferenceModule::class,
        BusesModule::class,
        FaresModule::class,
        FavouritesModule::class,
        NearbyModule::class,
        RoutesModule::class,
        ScanModule::class,
        SettingsModule::class,
        StopsModule::class,
        TrackingModule::class,
    ],
)
object LogronoBusKoinApp

fun initKoin(config: KoinAppDeclaration? = null) {
    val application =
        startKoin<LogronoBusKoinApp> {
            config?.invoke(this)
        }
}
