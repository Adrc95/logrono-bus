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

package dev.adrc95.logronobus.core.network.mapper

import dev.adrc95.logronobus.core.common.time.TimeConstants
import dev.adrc95.logronobus.core.common.time.parseInstant
import dev.adrc95.logronobus.core.model.FleetActivity
import dev.adrc95.logronobus.core.network.NetworkConstants.NO_LINE
import dev.adrc95.logronobus.core.network.dto.ActivityDto
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.time.Instant

fun ActivityDto.toDomain(): FleetActivity =
    FleetActivity(
        vehicleRef = vehicleRef,
        lineRef = lineRef,
        directionRef = directionRef,
        publishedLineName = publishedLineName.takeIf { it.isNotBlank() && it != NO_LINE },
        latitude = latitude,
        longitude = longitude,
        recordedAt = locationRecordedAtTime.parseInstant(),
        delaySeconds = delaySeconds,
        predictionInaccurate = predictionInaccurate,
        nextStopRef = nextStopRef.trim().trimStart('0').ifBlank { null },
        nextStopExpectedAt = nextStopExpectedArrivalTime.parseLocalStampAsInstant(),
        nextStopAimedAt = nextStopAimedArrivalTime.parseLocalStampAsInstant(),
    )

private fun String.parseLocalStampAsInstant(): Instant? {
    val text = this.trim().removeSuffix("Z").removeSuffix("z")
    if (text.isBlank()) return null
    return runCatching {
        LocalDateTime.parse(text).toInstant(TimeConstants.LOCAL_ZONE)
    }.getOrNull()
}
