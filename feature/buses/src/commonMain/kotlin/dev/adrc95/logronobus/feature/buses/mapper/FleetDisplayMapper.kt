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

package dev.adrc95.logronobus.feature.buses.mapper

import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable
import dev.adrc95.logronobus.core.ui.generated.resources.live_feed_unavailable_detail
import dev.adrc95.logronobus.core.ui.generated.resources.loading_live_buses
import dev.adrc95.logronobus.core.ui.generated.resources.no_buses_reporting
import dev.adrc95.logronobus.core.ui.generated.resources.service_may_not_be_running
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetStatus
import kotlinx.collections.immutable.toImmutableList

fun fleetDisplayModel(
    buses: List<BusLocationDisplayModel>,
    hasLoaded: Boolean,
    feedStale: Boolean,
    updatedMs: Long?,
): FleetDisplayModel =
    FleetDisplayModel(
        status = fleetStatus(buses = buses, hasLoaded = hasLoaded, feedStale = feedStale),
        updatedMs = updatedMs,
        buses = buses.toImmutableList(),
    )

private fun fleetStatus(
    buses: List<BusLocationDisplayModel>,
    hasLoaded: Boolean,
    feedStale: Boolean,
): FleetStatus =
    when {
        buses.isNotEmpty() -> {
            FleetStatus.Running
        }

        !hasLoaded -> {
            FleetStatus.Loading(Res.string.loading_live_buses)
        }

        feedStale -> {
            FleetStatus.Unavailable(
                title = Res.string.live_feed_unavailable,
                detail = Res.string.live_feed_unavailable_detail,
            )
        }

        else -> {
            FleetStatus.Empty(
                title = Res.string.no_buses_reporting,
                detail = Res.string.service_may_not_be_running,
            )
        }
    }
