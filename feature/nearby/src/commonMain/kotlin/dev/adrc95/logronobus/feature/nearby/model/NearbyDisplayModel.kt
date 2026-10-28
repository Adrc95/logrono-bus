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

package dev.adrc95.logronobus.feature.nearby.model

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface DistanceDisplayModel {
    data class Metres(
        val metres: Int,
    ) : DistanceDisplayModel

    data class Kilometres(
        val kilometres: Double,
    ) : DistanceDisplayModel
}

data class NearbyStopDisplayModel(
    val stop: StopDisplayModel,
    val distance: DistanceDisplayModel,
    val walkMinutes: Int?,
    val lineCodes: ImmutableList<String>,
    val lineColors: Map<String, Color>,
    val lineOrigins: Map<String, String>,
    val isFavourite: Boolean,
)

data class NearbyDisplayModel(
    val stops: ImmutableList<NearbyStopDisplayModel>,
    val isFallback: Boolean,
)
