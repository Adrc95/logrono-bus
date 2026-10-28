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

package dev.adrc95.logronobus.core.network.builder

import dev.adrc95.logronobus.core.network.dto.ActivityDto

class ActivityDtoBuilder {
    var vehicleRef: String = "sample"
    var lineRef: String = "sample"
    var directionRef: String = "sample"
    var publishedLineName: String = "sample"
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var locationRecordedAtTime: String = "sample"
    var delaySeconds: Int? = null
    var monitored: Boolean = false
    var predictionInaccurate: Boolean = false
    var confidenceLevel: String = "sample"
    var nextStopRef: String = "sample"
    var nextStopExpectedArrivalTime: String = "sample"
    var nextStopAimedArrivalTime: String = "sample"

    fun withVehicleRef(vehicleRef: String) = apply { this.vehicleRef = vehicleRef }

    fun withLineRef(lineRef: String) = apply { this.lineRef = lineRef }

    fun withDirectionRef(directionRef: String) = apply { this.directionRef = directionRef }

    fun withPublishedLineName(publishedLineName: String) = apply { this.publishedLineName = publishedLineName }

    fun withLatitude(latitude: Double) = apply { this.latitude = latitude }

    fun withLongitude(longitude: Double) = apply { this.longitude = longitude }

    fun withLocationRecordedAtTime(locationRecordedAtTime: String) = apply { this.locationRecordedAtTime = locationRecordedAtTime }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun withMonitored(monitored: Boolean) = apply { this.monitored = monitored }

    fun withPredictionInaccurate(predictionInaccurate: Boolean) = apply { this.predictionInaccurate = predictionInaccurate }

    fun withConfidenceLevel(confidenceLevel: String) = apply { this.confidenceLevel = confidenceLevel }

    fun withNextStopRef(nextStopRef: String) = apply { this.nextStopRef = nextStopRef }

    fun withNextStopExpectedArrivalTime(nextStopExpectedArrivalTime: String) = apply { this.nextStopExpectedArrivalTime = nextStopExpectedArrivalTime }

    fun withNextStopAimedArrivalTime(nextStopAimedArrivalTime: String) = apply { this.nextStopAimedArrivalTime = nextStopAimedArrivalTime }

    fun build() =
        ActivityDto(
            vehicleRef = vehicleRef,
            lineRef = lineRef,
            directionRef = directionRef,
            publishedLineName = publishedLineName,
            latitude = latitude,
            longitude = longitude,
            locationRecordedAtTime = locationRecordedAtTime,
            delaySeconds = delaySeconds,
            monitored = monitored,
            predictionInaccurate = predictionInaccurate,
            confidenceLevel = confidenceLevel,
            nextStopRef = nextStopRef,
            nextStopExpectedArrivalTime = nextStopExpectedArrivalTime,
            nextStopAimedArrivalTime = nextStopAimedArrivalTime,
        )
}

fun activityDto(block: ActivityDtoBuilder.() -> Unit = {}) = ActivityDtoBuilder().apply(block).build()
