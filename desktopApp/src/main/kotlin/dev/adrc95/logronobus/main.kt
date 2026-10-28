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

package dev.adrc95.logronobus

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.adrc95.logronobus.di.initKoin
import dev.adrc95.logronobus.ui.App
import org.jetbrains.skia.Image
import java.awt.Dimension
import java.util.prefs.Preferences

private val appIcon: BitmapPainter by lazy {
    val stream =
        checkNotNull(
            Thread.currentThread().contextClassLoader?.getResourceAsStream("app-icon.png"),
        )
    BitmapPainter(Image.makeFromEncoded(stream.use { it.readBytes() }).toComposeImageBitmap())
}

private val windowPrefs: Preferences =
    Preferences.userRoot().node("dev/adrc95/logronobus/window")

fun main() {
    initKoin()
    application {
        val state =
            rememberWindowState(
                width = windowPrefs.getInt("width", 1000).dp,
                height = windowPrefs.getInt("height", 720).dp,
                position =
                    windowPrefs.getInt("x", Int.MIN_VALUE).let { x ->
                        if (x == Int.MIN_VALUE) {
                            WindowPosition.PlatformDefault
                        } else {
                            WindowPosition(x.dp, windowPrefs.getInt("y", 0).dp)
                        }
                    },
            )
        Window(
            onCloseRequest = {
                windowPrefs.putInt(
                    "width",
                    state.size.width.value
                        .toInt(),
                )
                windowPrefs.putInt(
                    "height",
                    state.size.height.value
                        .toInt(),
                )
                (state.position as? WindowPosition.Absolute)?.let {
                    windowPrefs.putInt("x", it.x.value.toInt())
                    windowPrefs.putInt("y", it.y.value.toInt())
                }
                exitApplication()
            },
            title = "Logroño Bus",
            state = state,
            icon = appIcon,
        ) {
            LaunchedEffect(Unit) { window.minimumSize = Dimension(480, 480) }
            App()
        }
    }
}
