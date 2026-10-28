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

import dev.adrc95.logronobus.core.network.dto.ApiLinesResponse
import dev.adrc95.logronobus.core.network.dto.ApiLinesResult
import dev.adrc95.logronobus.core.network.dto.ApiStopsResponse
import dev.adrc95.logronobus.core.network.dto.ApiStopsResult
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.StopDto
import dev.adrc95.logronobus.core.network.service.CatalogueService

class FakeCatalogueService(
    var lines: List<LineDto> = emptyList(),
    var stops: List<StopDto> = emptyList(),
) : CatalogueService {
    override suspend fun getStops(): ApiStopsResponse = ApiStopsResponse(ApiStopsResult(stops))

    override suspend fun getLines(): ApiLinesResponse = ApiLinesResponse(ApiLinesResult(lines))
}
