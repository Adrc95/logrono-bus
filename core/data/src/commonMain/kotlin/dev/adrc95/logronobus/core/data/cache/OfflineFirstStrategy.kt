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

package dev.adrc95.logronobus.core.data

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [CacheStrategy::class])
@Named("OfflineFirst")
class OfflineFirstStrategy : CacheStrategy {
    override suspend fun <T> execute(
        remote: suspend () -> T,
        local: suspend () -> T,
        save: suspend (T) -> Unit,
        isExpired: suspend () -> Boolean,
    ): T {
        try {
            if (!isExpired()) {
                val localResult = local()
                if (localResult != null &&
                    (localResult !is Collection<*> || localResult.isNotEmpty()) &&
                    (localResult !is Map<*, *> || localResult.isNotEmpty())
                ) {
                    return localResult
                }
            }
            val remoteResult = remote()
            save(remoteResult)
            return remoteResult
        } catch (e: Exception) {
            return try {
                local()
            } catch (localEx: Exception) {
                throw e
            }
        }
    }
}
