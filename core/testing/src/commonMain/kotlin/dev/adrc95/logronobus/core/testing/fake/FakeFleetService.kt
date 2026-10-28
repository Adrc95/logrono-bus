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

package dev.adrc95.logronobus.core.testing.fake

import dev.adrc95.logronobus.core.network.dto.ActivityDto
import dev.adrc95.logronobus.core.network.dto.ApiFleetResponse
import dev.adrc95.logronobus.core.network.dto.ApiFleetResult
import dev.adrc95.logronobus.core.network.service.FleetService

class FakeFleetService(
    var activities: List<ActivityDto> = emptyList(),
    var failing: Boolean = false,
) : FleetService {
    var calls = 0
        private set

    var lineCalls = 0
        private set

    override suspend fun getFleet(): ApiFleetResponse {
        calls++
        return answer()
    }

    override suspend fun getLineFleet(lineId: Int): ApiFleetResponse {
        lineCalls++
        return answer()
    }

    private fun answer(): ApiFleetResponse {
        if (failing) error("the fleet feed is unavailable")
        return ApiFleetResponse(ApiFleetResult(activities))
    }
}
