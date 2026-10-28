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

fun String.withoutCodePrefix(): String =
    substringAfter('-', missingDelimiterValue = "")
        .trim()
        .ifEmpty { this }

fun String.routeName(): String =
    split("-")
        .drop(1)
        .map { part -> part.trim() }
        .filter { part -> part.isNotEmpty() }
        .joinToString(" - ") { part -> part.titleCased() }
        .ifEmpty { titleCased() }

fun String.titleCased(): String =
    lowercase()
        .split(" ")
        .mapIndexed { index, word ->
            when {
                word.isEmpty() -> word
                index == 0 || word !in TITLE_CONNECTORS -> word.replaceFirstChar { it.uppercase() }
                else -> word
            }
        }.joinToString(" ")

internal val TITLE_CONNECTORS =
    setOf(
        "de",
        "del",
        "la",
        "las",
        "el",
        "los",
        "y",
        "e",
        "o",
        "u",
        "a",
        "en",
        "con",
        "por",
        "al",
    )
