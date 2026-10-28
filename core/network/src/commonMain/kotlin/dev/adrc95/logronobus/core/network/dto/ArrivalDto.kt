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

package dev.adrc95.logronobus.core.network.dto

import dev.adrc95.logronobus.core.network.NetworkConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArrivalDto(
    @SerialName(NetworkConstants.FIELD_LINE_REF)
    val lineRef: String,
    @SerialName(NetworkConstants.FIELD_DIRECTION_REF)
    val directionRef: String,
    @SerialName(NetworkConstants.FIELD_JOURNEY_CODE)
    val journeyCode: String,
    @SerialName(NetworkConstants.FIELD_VEHICLE_REF)
    val vehicleRef: String,
    @SerialName(NetworkConstants.FIELD_STOP_POINT_REF)
    val stopPointRef: String,
    @SerialName(NetworkConstants.FIELD_ORDER)
    val order: Int?,
    @SerialName(NetworkConstants.FIELD_AIMED_ARRIVAL_TIME)
    val aimedArrivalTime: String,
    @SerialName(NetworkConstants.FIELD_EXPECTED_ARRIVAL_TIME)
    val expectedArrivalTime: String,
    @SerialName(NetworkConstants.FIELD_AIMED_DEPARTURE_TIME)
    val aimedDepartureTime: String,
    @SerialName(NetworkConstants.FIELD_EXPECTED_DEPARTURE_TIME)
    val expectedDepartureTime: String,
    @SerialName(NetworkConstants.FIELD_ARRIVAL_STATUS)
    val arrivalStatus: String,
    @SerialName(NetworkConstants.FIELD_DEPARTURE_STATUS)
    val departureStatus: String,
    @SerialName(NetworkConstants.FIELD_CANCELLATION)
    val cancellation: Boolean,
    @SerialName(NetworkConstants.FIELD_PREDICTION_INACCURATE)
    val predictionInaccurate: Boolean,
    @SerialName(NetworkConstants.FIELD_DELAY_SECONDS)
    val delaySeconds: Int?,
)
