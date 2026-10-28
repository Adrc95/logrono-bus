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

import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.LocationResult
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidLocationProvider(
    private val context: Context,
    private val requestPermission: suspend () -> Boolean,
) : LocationProvider {
    override suspend fun currentLocation(): LocationResult {
        if (!requestPermission()) return LocationResult.PermissionDenied
        return try {
            awaitCurrentLocation(context)
        } catch (_: SecurityException) {
            LocationResult.PermissionDenied
        }
    }

    private suspend fun awaitCurrentLocation(context: Context): LocationResult =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request =
                CurrentLocationRequest
                    .Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .build()
            val cancellation = CancellationTokenSource()
            cont.invokeOnCancellation { _ -> cancellation.cancel() }
            client
                .getCurrentLocation(request, cancellation.token)
                .addOnSuccessListener { location ->
                    if (!cont.isActive) return@addOnSuccessListener
                    cont.resume(
                        if (location != null) {
                            LocationResult.Available(
                                GeoPoint(
                                    location.latitude,
                                    location.longitude,
                                ),
                            )
                        } else {
                            LocationResult.Unavailable
                        },
                    )
                }.addOnFailureListener {
                    if (cont.isActive) cont.resume(LocationResult.Unavailable)
                }
        }
}
