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

package dev.adrc95.logronobus.core.data.repository

import dev.adrc95.logronobus.core.common.exception.AppErrorException
import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.data.CacheStrategy
import dev.adrc95.logronobus.core.database.datasource.FaresLocalDataSource
import dev.adrc95.logronobus.core.domain.repository.FaresRepository
import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.network.datasource.FaresDataSource
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [FaresRepository::class])
class FaresRepositoryImpl(
    private val remote: FaresDataSource,
    private val local: FaresLocalDataSource? = null,
    @Named("OfflineFirst")
    private val cache: CacheStrategy? = null,
) : FaresRepository {
    override suspend fun getFareProducts(): List<Fare> =
        if (cache != null && local != null) {
            cache.execute(
                remote = {
                    when (val answer = remote.fareProducts()) {
                        is Result.Success -> answer.data
                        is Result.Error -> throw AppErrorException(answer.error)
                    }
                },
                local = { local.getFares() },
                save = { local.saveFares(it) },
                isExpired = { local.isExpired() },
            )
        } else {
            when (val answer = remote.fareProducts()) {
                is Result.Success -> answer.data
                is Result.Error -> throw AppErrorException(answer.error)
            }
        }
}
