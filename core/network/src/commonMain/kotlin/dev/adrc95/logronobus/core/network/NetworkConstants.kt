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

package dev.adrc95.logronobus.core.network

object NetworkConstants {
    const val BASE_URL = "https://transporteurbano.logrono.es/"
    const val CATALOGUE_STOPS = "api/linesDiscovery/stops"
    const val CATALOGUE_LINES = "api/linesDiscovery/lines"
    const val NO_LINE = "[object Object]"
    const val CONNECT_TIMEOUT_MILLIS = 30_000L
    const val REQUEST_TIMEOUT_MILLIS = 30_000L
    const val SOCKET_TIMEOUT_MILLIS = 30_000L
    const val VEHICLE_CACHE_TTL_MILLIS = 30_000L
    const val STALE_POSITIONS_MAX_AGE_MILLIS = 3L * 60 * 1000
    const val DEFAULT_FRESH_MINUTES = 5L
    const val RETRY_ATTEMPTS = 3
    const val RETRY_BASE_DELAY_MILLIS = 400L
    const val TWIN_KERB_METRES = 80.0
    const val DIRECTION_REF_ASC = "ida"
    const val DIRECTION_REF_DESC = "vuelta"
    const val END_SHARE = 0.85
    const val START_SHARE = 0.15
    const val MAX_LINES_PER_REQUEST = 4
    const val FIELD_ACTIVITIES = "activities"
    const val FIELD_AIMED_ARRIVAL_TIME = "aimedArrivalTime"
    const val FIELD_AIMED_DEPARTURE_TIME = "aimedDepartureTime"
    const val FIELD_ARRIVAL_STATUS = "arrivalStatus"
    const val FIELD_ARRIVALS = "arrivals"
    const val FIELD_ASC = "asc"
    const val FIELD_CANCELLATION = "cancellation"
    const val FIELD_COLOR = "color"
    const val FIELD_CONFIDENCE_LEVEL = "confidenceLevel"
    const val FIELD_DELAY_SECONDS = "delaySeconds"
    const val FIELD_DEPARTURE_STATUS = "departureStatus"
    const val FIELD_DESC = "desc"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_DESTINATIONS = "destinations"
    const val FIELD_DIRECTION_REF = "directionRef"
    const val FIELD_DISTANCE_M = "distanceM"
    const val FIELD_DURATION_SEC = "durationSec"
    const val FIELD_EXPECTED_ARRIVAL_TIME = "expectedArrivalTime"
    const val FIELD_EXPECTED_DEPARTURE_TIME = "expectedDepartureTime"
    const val FIELD_ID = "id"
    const val FIELD_ITEMS = "items"
    const val FIELD_JOURNEY_CODE = "journeyCode"
    const val FIELD_LAT = "lat"
    const val FIELD_LATITUDE = "latitude"
    const val FIELD_LINE_REF = "lineRef"
    const val FIELD_LINES = "lines"
    const val FIELD_LNG = "lng"
    const val FIELD_LOCATION_RECORDED_AT_TIME = "locationRecordedAtTime"
    const val FIELD_LONGITUDE = "longitude"
    const val FIELD_MAX_QUANTITY = "maxQuantity"
    const val FIELD_MONITORED = "monitored"
    const val FIELD_NAME = "name"
    const val FIELD_NEXT_STOP_AIMED_ARRIVAL_TIME = "nextStopAimedArrivalTime"
    const val FIELD_NEXT_STOP_EXPECTED_ARRIVAL_TIME = "nextStopExpectedArrivalTime"
    const val FIELD_NEXT_STOP_REF = "nextStopRef"
    const val FIELD_ONWARD_SHAPE = "onwardShape"
    const val FIELD_ORDER = "order"
    const val FIELD_ORIGIN = "origin"
    const val FIELD_PASSES = "passes"
    const val FIELD_PASSES_BY_DIRECTION = "passesByDirection"
    const val FIELD_PREDICTION_INACCURATE = "predictionInaccurate"
    const val FIELD_PRICE = "price"
    const val FIELD_PRODUCT_CODE = "productCode"
    const val FIELD_PRODUCTS = "products"
    const val FIELD_PUBLISHED_LINE_NAME = "publishedLineName"
    const val FIELD_RESULT = "result"
    const val FIELD_STATUS = "status"
    const val FIELD_STOP_POINT_REF = "stopPointRef"
    const val FIELD_STOPS = "stops"
    const val FIELD_VEHICLE_REF = "vehicleRef"
}
