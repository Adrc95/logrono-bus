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

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.net.HttpURLConnection
import java.net.URI

internal fun tileUrl(id: TileId): String = "$TILE_URL_BASE/${id.z}/${id.x}/${id.y}.png"

internal fun fetchTile(url: String): ByteArray? {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = TILE_TIMEOUT_MILLIS
        connection.readTimeout = TILE_TIMEOUT_MILLIS
        connection.setRequestProperty("User-Agent", TILE_USER_AGENT)
        if (connection.responseCode !in 200..299) {
            null
        } else {
            connection.inputStream.use { it.readBytes() }
        }
    } finally {
        connection.disconnect()
    }
}

internal fun ByteArray.toImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()
