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

package dev.adrc95.logronobus.core.preference

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

@Suppress("UNCHECKED_CAST")
inline fun <reified T> preferenceKey(name: String): Preferences.Key<T> {
    val key: Preferences.Key<*> =
        when (T::class) {
            String::class -> stringPreferencesKey(name)
            Boolean::class -> booleanPreferencesKey(name)
            Int::class -> intPreferencesKey(name)
            Long::class -> longPreferencesKey(name)
            Float::class -> floatPreferencesKey(name)
            Double::class -> doublePreferencesKey(name)
            Set::class -> stringSetPreferencesKey(name)
            else -> error("A preference cannot hold a ${T::class.simpleName}")
        }
    return key as Preferences.Key<T>
}
