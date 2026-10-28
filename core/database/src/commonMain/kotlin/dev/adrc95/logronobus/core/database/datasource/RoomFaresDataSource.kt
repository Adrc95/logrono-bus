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

package dev.adrc95.logronobus.core.database.datasource

import dev.adrc95.logronobus.core.database.DatabaseConstants
import dev.adrc95.logronobus.core.database.dao.FaresDao
import dev.adrc95.logronobus.core.database.mapper.toDomain
import dev.adrc95.logronobus.core.database.mapper.toEntity
import dev.adrc95.logronobus.core.model.Fare
import org.koin.core.annotation.Single

@Single(binds = [FaresLocalDataSource::class])
class RoomFaresDataSource(
    private val dao: FaresDao,
) : FaresLocalDataSource {
    override suspend fun getFares(): List<Fare> {
        val rows = dao.all()
        if (rows.isEmpty()) return emptyList()
        return rows.map { it.toDomain() }
    }

    override suspend fun saveFares(fares: List<Fare>) {
        dao.replaceAll(
            fares.map { it.toEntity() },
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds(),
        )
    }

    override suspend fun isExpired(): Boolean {
        val storedAt = dao.storedAt() ?: return true
        return kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds() - storedAt > DatabaseConstants.FARES_TTL_MILLIS
    }
}
