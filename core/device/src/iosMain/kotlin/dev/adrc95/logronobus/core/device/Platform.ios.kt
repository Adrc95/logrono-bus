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

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val appVersion: String =
        run {
            val bundle = NSBundle.mainBundle
            val short = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            when {
                short != null && build != null -> "$short ($build)"
                short != null -> short
                else -> "unknown"
            }
        }
}
