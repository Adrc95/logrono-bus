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

import dev.adrc95.logronobus.core.network.dto.FareProductDto

class FareProductDtoBuilder {
    var productCode: String = "sample"
    var name: String = "sample"
    var price: Int? = null
    var description: String? = null
    var maxQuantity: Int = 0

    fun withProductCode(productCode: String) = apply { this.productCode = productCode }

    fun withName(name: String) = apply { this.name = name }

    fun withPrice(price: Int?) = apply { this.price = price }

    fun withDescription(description: String?) = apply { this.description = description }

    fun withMaxQuantity(maxQuantity: Int) = apply { this.maxQuantity = maxQuantity }

    fun build() =
        FareProductDto(
            productCode = productCode,
            name = name,
            price = price,
            description = description,
            maxQuantity = maxQuantity,
        )
}

fun fareProductDto(block: FareProductDtoBuilder.() -> Unit = {}) = FareProductDtoBuilder().apply(block).build()
