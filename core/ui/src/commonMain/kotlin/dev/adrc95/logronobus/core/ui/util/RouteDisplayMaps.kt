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

package dev.adrc95.logronobus.core.ui.util

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.model.Route

fun List<Route>.toRouteColors(): Map<String, Color> =
    mapNotNull { route ->
        route.color?.let { rgba -> rgba.toLineColor()?.let { route.shortName to it } }
    }.toMap()

fun List<Route>.toRouteOrigins(): Map<String, String> =
    associate { route ->
        val first =
            route.longName
                .split("-")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .drop(1)
                .firstOrNull()
        route.shortName to (first?.titleCased() ?: route.shortName)
    }

fun routeCodeComparator(): Comparator<String> =
    Comparator { a, b ->
        val an = a.toIntOrNull()
        val bn = b.toIntOrNull()
        when {
            an != null && bn != null -> an.compareTo(bn)
            an != null -> -1
            bn != null -> 1
            else -> a.compareTo(b)
        }
    }
