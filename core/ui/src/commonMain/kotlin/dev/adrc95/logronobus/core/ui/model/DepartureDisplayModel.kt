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

package dev.adrc95.logronobus.core.ui.model

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.model.DepartureTime

sealed interface DepartureDestination {
    data class Towards(
        val name: String,
    ) : DepartureDestination

    data object StartsHere : DepartureDestination

    data object TerminatesHere : DepartureDestination
}

data class DepartureDisplayModel(
    val lineCode: String,
    val destination: DepartureDestination,
    val departure: DepartureTime,
    val isTrackable: Boolean,
    val vehicleId: String? = null,
    val lineColor: Color? = null,
)
