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

package dev.adrc95.logronobus.core.data.repository

import dev.adrc95.logronobus.core.domain.repository.TrackedTripRepository
import dev.adrc95.logronobus.core.model.TrackedTrip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [TrackedTripRepository::class])
class TrackedTripRepositoryImpl : TrackedTripRepository {
    private val _tracked = MutableStateFlow<TrackedTrip?>(null)

    override val tracked: StateFlow<TrackedTrip?> = _tracked.asStateFlow()

    override fun update(tracked: TrackedTrip?) {
        _tracked.value = tracked
    }
}
