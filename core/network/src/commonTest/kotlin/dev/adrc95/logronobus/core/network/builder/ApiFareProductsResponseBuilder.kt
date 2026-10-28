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

package dev.adrc95.logronobus.core.network.builder

import dev.adrc95.logronobus.core.network.dto.ApiFareProductsResponse
import dev.adrc95.logronobus.core.network.dto.ApiFareProductsResult
import dev.adrc95.logronobus.core.network.dto.FareProductDto

class ApiFareProductsResponseBuilder {
    var result: ApiFareProductsResult = ApiFareProductsResultBuilder().build()

    fun withResult(result: ApiFareProductsResult) = apply { this.result = result }

    fun build() = ApiFareProductsResponse(result = result)
}

fun apiFareProductsResponse(block: ApiFareProductsResponseBuilder.() -> Unit = {}) = ApiFareProductsResponseBuilder().apply(block).build()

class ApiFareProductsResultBuilder {
    var products: List<FareProductDto> = emptyList()

    fun withProducts(products: List<FareProductDto>) = apply { this.products = products }

    fun build() = ApiFareProductsResult(products = products)
}

fun apiFareProductsResult(block: ApiFareProductsResultBuilder.() -> Unit = {}) = ApiFareProductsResultBuilder().apply(block).build()
