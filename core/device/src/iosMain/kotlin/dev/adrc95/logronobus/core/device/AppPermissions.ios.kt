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

package dev.adrc95.logronobus.core.device

import dev.brewkits.grant.AppGrant
import dev.brewkits.grant.GrantFactory
import dev.brewkits.grant.GrantManager
import dev.brewkits.grant.GrantStatus

internal class IosAppPermissions : AppPermissions {
    private val manager: GrantManager by lazy { GrantFactory.create() }

    override suspend fun requestCamera(): Boolean = request(AppGrant.CAMERA)

    override suspend fun requestLocation(): Boolean = request(AppGrant.LOCATION)

    private suspend fun request(grant: AppGrant): Boolean = manager.request(grant) == GrantStatus.GRANTED
}
