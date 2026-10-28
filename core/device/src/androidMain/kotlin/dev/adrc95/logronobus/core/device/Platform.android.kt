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

package dev.adrc95.logronobus.core.device

import android.content.Context
import android.os.Build

class AndroidPlatform(
    private val context: Context,
) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val appVersion: String =
        run {
            try {
                val info = context.packageManager.getPackageInfo(context.packageName, 0)

                @Suppress("DEPRECATION")
                val code =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        info.longVersionCode
                    } else {
                        info.versionCode.toLong()
                    }
                "${info.versionName} ($code)"
            } catch (_: Exception) {
                "unknown"
            }
        }
}
