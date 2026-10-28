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

import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.LocationResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosLocationProvider : LocationProvider {
    private var manager: CLLocationManager? = null
    private var delegate: LocationDelegate? = null

    override suspend fun currentLocation(): LocationResult =
        suspendCancellableCoroutine { cont ->
            val locationManager = CLLocationManager()
            val locationDelegate =
                LocationDelegate { result ->
                    if (cont.isActive) cont.resumeWith(Result.success(result))
                }
            locationManager.delegate = locationDelegate
            manager = locationManager
            delegate = locationDelegate

            cont.invokeOnCancellation { locationManager.delegate = null }

            when (locationManager.authorizationStatus) {
                kCLAuthorizationStatusNotDetermined -> {
                    locationManager.requestWhenInUseAuthorization()
                }

                kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                    if (cont.isActive) {
                        cont.resumeWith(Result.success(LocationResult.PermissionDenied))
                    }
                }

                else -> {
                    locationManager.requestLocation()
                }
            }
        }

    private class LocationDelegate(
        private val onResult: InputAction<LocationResult>,
    ) : NSObject(),
        CLLocationManagerDelegateProtocol {
        private var finished = false

        private fun finish(result: LocationResult) {
            if (finished) return
            finished = true
            onResult(result)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways,
                -> {
                    manager.requestLocation()
                }

                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted,
                -> {
                    finish(LocationResult.PermissionDenied)
                }

                else -> {}
            }
        }

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>,
        ) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            if (location != null) {
                val coordinate = location.coordinate
                val lat = coordinate.useContents { latitude }
                val lon = coordinate.useContents { longitude }
                finish(LocationResult.Available(GeoPoint(lat, lon)))
            } else {
                finish(LocationResult.Unavailable)
            }
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: NSError,
        ) {
            finish(LocationResult.Unavailable)
        }
    }
}
