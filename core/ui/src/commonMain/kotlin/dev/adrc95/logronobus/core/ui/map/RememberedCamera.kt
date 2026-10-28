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

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.runtime.saveable.Saver
import dev.adrc95.logronobus.core.common.InputOutputAction

class RememberedCamera<T>(
    var position: T? = null,
)

fun cameraString(
    latitude: Double,
    longitude: Double,
    zoom: Double,
    bearing: Double,
    tilt: Double,
): String = listOf(latitude, longitude, zoom, bearing, tilt).joinToString(",")

private fun parseCamera(raw: String?): DoubleArray? =
    raw
        ?.split(",")
        ?.mapNotNull { it.trim().toDoubleOrNull() }
        ?.takeIf { it.size == 5 }
        ?.toDoubleArray()

fun <T> rememberedCameraSaver(
    save: InputOutputAction<T, String>,
    restore: InputOutputAction<DoubleArray, T?>,
): Saver<RememberedCamera<T>, String> =
    Saver(
        save = { holder -> holder.position?.let(save) ?: "" },
        restore = { raw -> RememberedCamera(parseCamera(raw)?.let(restore)) },
    )
