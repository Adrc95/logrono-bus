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

package dev.adrc95.logronobus.core.common.exception

import dev.adrc95.logronobus.core.common.InputOutputAction

sealed class Result<out R> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val error: AppError,
    ) : Result<Nothing>()
}

fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

fun <T> Result<T>.orThrow(): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> throw AppErrorException(error)
    }

inline fun <T, R> Result<T>.map(transform: InputOutputAction<T, R>): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }

fun Throwable.toAppError(): AppError = (this as? AppErrorException)?.error ?: AppError.Unknown(message.orEmpty())
