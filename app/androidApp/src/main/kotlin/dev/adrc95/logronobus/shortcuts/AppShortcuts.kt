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

package dev.adrc95.logronobus.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.net.toUri
import dev.adrc95.logronobus.MainActivity
import dev.adrc95.logronobus.R

private val appShortcuts =
    listOf(
        AppShortcut(
            id = "routes",
            shortLabel = R.string.shortcut_routes_short,
            longLabel = R.string.shortcut_routes_long,
            icon = R.drawable.ic_shortcut_routes,
            deepLink = "logronobus://routes",
        ),
        AppShortcut(
            id = "nearby",
            shortLabel = R.string.shortcut_nearby_short,
            longLabel = R.string.shortcut_nearby_long,
            icon = R.drawable.ic_shortcut_nearby,
            deepLink = "logronobus://nearby",
        ),
        AppShortcut(
            id = "scan",
            shortLabel = R.string.shortcut_scan_short,
            longLabel = R.string.shortcut_scan_long,
            icon = R.drawable.ic_shortcut_scan,
            deepLink = "logronobus://scan",
        ),
        AppShortcut(
            id = "favourites",
            shortLabel = R.string.shortcut_favourites_short,
            longLabel = R.string.shortcut_favourites_long,
            icon = R.drawable.ic_shortcut_favourites,
            deepLink = "logronobus://favourites",
        ),
    )

fun publishAppShortcuts(context: Context) {
    val manager = context.getSystemService(ShortcutManager::class.java) ?: return
    manager.dynamicShortcuts = appShortcuts.map { it.toShortcutInfo(context) }
}

private fun AppShortcut.toShortcutInfo(context: Context): ShortcutInfo =
    ShortcutInfo
        .Builder(context, id)
        .setShortLabel(context.getString(shortLabel))
        .setLongLabel(context.getString(longLabel))
        .setIcon(Icon.createWithResource(context, icon))
        .setIntent(
            Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(deepLink.toUri()),
        ).build()
