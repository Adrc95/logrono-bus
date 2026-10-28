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

package dev.adrc95.logronobus.core.database

object DatabaseConstants {
    const val FILE_NAME = "logronobus.db"
    const val SNAPSHOT_ROW = 0
    const val CATALOGUE_TTL_MILLIS = 24L * 60 * 60 * 1000
    const val FARES_TTL_MILLIS = 30L * 24 * 60 * 60 * 1000
    const val SOURCE_STOPS = "stops"
    const val SOURCE_LINES = "lines"
    const val TABLE_FAVOURITE_STOPS = "favourite_stops"
    const val TABLE_LEARNED_DIRECTIONS = "learned_directions"
    const val TABLE_CATALOGUE_STOP = "catalogue_stop"
    const val TABLE_CATALOGUE_STOP_LINE = "catalogue_stop_line"
    const val TABLE_CATALOGUE_LINE = "catalogue_line"
    const val TABLE_CATALOGUE_LINE_STOP = "catalogue_line_stop"
    const val TABLE_CATALOGUE_SYNC = "catalogue_sync"
    const val TABLE_FARES = "fares"
    const val COLUMN_CODE = "code"
    const val COLUMN_COLOUR = "colour"
    const val COLUMN_DESCRIPTION = "description"
    const val COLUMN_DIRECTION = "direction"
    const val COLUMN_DIRECTION_REF = "directionRef"
    const val COLUMN_HEADSIGN = "headsign"
    const val COLUMN_ID = "id"
    const val COLUMN_KIND = "kind"
    const val COLUMN_LAT = "lat"
    const val COLUMN_LINE_CODE = "lineCode"
    const val COLUMN_LINE_ID = "lineId"
    const val COLUMN_LNG = "lng"
    const val COLUMN_MUNICIPALITY = "municipality"
    const val COLUMN_NAME = "name"
    const val COLUMN_POSITION = "position"
    const val COLUMN_PRICE_CENTS = "priceCents"
    const val COLUMN_SHAPE = "shape"
    const val COLUMN_SOURCE = "source"
    const val COLUMN_STATUS = "status"
    const val COLUMN_STOP_ID = "stopId"
    const val COLUMN_STOP_REF = "stopRef"
    const val COLUMN_STORED_AT = "storedAt"
    const val COLUMN_TRAVELLER = "traveller"
}
