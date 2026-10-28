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

package dev.adrc95.logronobus.core.device.di

import android.content.Context
import dev.adrc95.logronobus.core.device.AndroidAppPermissions
import dev.adrc95.logronobus.core.device.AndroidLocationProvider
import dev.adrc95.logronobus.core.device.AndroidPlatform
import dev.adrc95.logronobus.core.device.AppPermissions
import dev.adrc95.logronobus.core.device.LocationProvider
import dev.adrc95.logronobus.core.device.Platform
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class DeviceProviderModule {
    @Single
    fun providePlatform(context: Context): Platform = AndroidPlatform(context)

    @Single
    fun provideAppPermissions(context: Context): AppPermissions = AndroidAppPermissions(context)

    @Single
    fun provideLocationProvider(
        context: Context,
        permissions: AppPermissions,
    ): LocationProvider = AndroidLocationProvider(context, permissions::requestLocation)
}
