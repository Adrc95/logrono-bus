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

package dev.adrc95.logronobus.feature.tracking.model

sealed interface DelayStatus {
    data object OnTime : DelayStatus

    data class Late(
        val minutes: Int,
    ) : DelayStatus

    data class Early(
        val minutes: Int,
    ) : DelayStatus

    data object Unknown : DelayStatus

    companion object {
        fun from(delaySeconds: Int?): DelayStatus =
            when {
                delaySeconds == null -> Unknown
                delaySeconds > 30 -> Late((delaySeconds + 30) / 60)
                delaySeconds < -30 -> Early((-delaySeconds + 30) / 60)
                else -> OnTime
            }
    }
}
