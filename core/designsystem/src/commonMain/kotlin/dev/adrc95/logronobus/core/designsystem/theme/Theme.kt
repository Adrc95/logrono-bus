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

package dev.adrc95.logronobus.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isDarkTheme(): Boolean = LocalDarkTheme.current

val LightColors =
    lightColorScheme(
        primary = Red50,
        onPrimary = White,
        primaryContainer = Pink90,
        onPrimaryContainer = Red10,
        inversePrimary = Pink80,
        secondary = Grey39,
        onSecondary = White,
        secondaryContainer = Grey90,
        onSecondaryContainer = Grey10,
        tertiary = Red60,
        onTertiary = White,
        tertiaryContainer = Salmon90,
        onTertiaryContainer = Red10,
        error = Red40,
        onError = White,
        errorContainer = Salmon90,
        onErrorContainer = Red10,
        background = Grey95,
        onBackground = Grey10,
        surface = White,
        onSurface = Grey10,
        surfaceVariant = Grey90,
        onSurfaceVariant = Grey39,
        surfaceContainerLowest = White,
        surfaceContainerLow = White,
        surfaceContainer = Grey95,
        surfaceContainerHigh = Grey92,
        surfaceContainerHighest = Grey90,
        surfaceBright = White,
        surfaceDim = Grey87,
        outline = Grey57,
        outlineVariant = Grey86,
        inverseSurface = Grey19,
        inverseOnSurface = Grey95,
        scrim = Black,
    )

val DarkColors =
    darkColorScheme(
        primary = Red50,
        onPrimary = White,
        primaryContainer = Pink20,
        onPrimaryContainer = Pink90,
        inversePrimary = Pink90,
        secondary = Grey81,
        onSecondary = Grey19,
        secondaryContainer = Grey29,
        onSecondaryContainer = Grey90,
        tertiary = Pink60,
        onTertiary = White,
        tertiaryContainer = Red30,
        onTertiaryContainer = Salmon90,
        error = Pink60,
        onError = White,
        errorContainer = Red30,
        onErrorContainer = Salmon90,
        background = Black,
        onBackground = Grey92,
        surface = Black,
        onSurface = Grey92,
        surfaceVariant = Grey17,
        onSurfaceVariant = Grey77,
        surfaceContainerLowest = Black,
        surfaceContainerLow = Grey1,
        surfaceContainer = Grey5,
        surfaceContainerHigh = Grey10,
        surfaceContainerHighest = Grey13,
        surfaceBright = Grey22,
        surfaceDim = Black,
        outline = Grey57,
        outlineVariant = Grey17,
        inverseSurface = Grey92,
        inverseOnSurface = Grey10,
        scrim = Black,
    )

@Composable
fun LogronoBusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    SystemBarsAppearance(darkTheme)
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LogronoTypography,
            shapes = LogronoShapes,
        ) {
            Surface(
                color = colorScheme.background,
                contentColor = colorScheme.onBackground,
                content = content,
            )
        }
    }
}
