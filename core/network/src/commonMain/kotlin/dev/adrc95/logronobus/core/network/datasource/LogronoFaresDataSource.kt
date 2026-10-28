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

package dev.adrc95.logronobus.core.network.datasource

import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.common.exception.map
import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.service.FaresService
import dev.adrc95.logronobus.core.network.tryCall
import org.koin.core.annotation.Factory

@Factory(binds = [FaresDataSource::class])
class LogronoFaresDataSource(
    private val service: FaresService,
) : FaresDataSource {
    override suspend fun fareProducts(): Result<List<Fare>> =
        tryCall { service.getFareProducts() }
            .map { response -> response.result.products.map { product -> product.toDomain() } }
}
