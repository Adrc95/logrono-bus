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

import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.LocationResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (onSuccess, onError) => {
        if (!navigator.geolocation) { onError(0); return; }
        navigator.geolocation.getCurrentPosition(
            (pos) => onSuccess(pos.coords.latitude, pos.coords.longitude),
            (err) => onError(err.code)
        );
    }
    """,
)
private external fun requestGeolocation(
    onSuccess: DoubleInputAction<Double, Double>,
    onError: InputAction<Int>,
)

class WasmLocationProvider : LocationProvider {
    override suspend fun currentLocation(): LocationResult =
        suspendCancellableCoroutine { cont ->
            requestGeolocation(
                onSuccess = { lat, lon ->
                    if (cont.isActive) {
                        cont.resume(LocationResult.Available(GeoPoint(lat, lon)), onCancellation = null)
                    }
                },
                onError = { code ->
                    if (cont.isActive) {
                        cont.resume(
                            if (code == 1) LocationResult.PermissionDenied else LocationResult.Unavailable,
                            onCancellation = null,
                        )
                    }
                },
            )
        }
}
