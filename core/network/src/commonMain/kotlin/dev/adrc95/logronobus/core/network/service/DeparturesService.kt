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

package dev.adrc95.logronobus.core.network.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import dev.adrc95.logronobus.core.network.dto.ApiLineTimetableResponse
import dev.adrc95.logronobus.core.network.dto.ApiPassesResponse
import dev.adrc95.logronobus.core.network.dto.ApiTimetableResponse

interface DeparturesService {
    @GET("api/estimatedTimetable/byStop/{stopId}")
    suspend fun getArrivals(
        @Path("stopId") stopId: Int,
        @Query("lines") lines: String,
        @Query("previewMinutes") previewMinutes: Int,
    ): ApiTimetableResponse

    @GET("api/estimatedTimetable/byLine/{lineId}")
    suspend fun getLineArrivals(
        @Path("lineId") lineId: Int,
        @Query("lines") lines: String,
    ): ApiTimetableResponse

    @GET("api/productionTimetable/byStop/{stopId}")
    suspend fun getPasses(
        @Path("stopId") stopId: Int,
        @Query("lines") lines: String,
    ): ApiPassesResponse

    @GET("api/productionTimetable/byLine/{lineId}")
    suspend fun getLineTimetable(
        @Path("lineId") lineId: Int,
    ): ApiLineTimetableResponse
}
