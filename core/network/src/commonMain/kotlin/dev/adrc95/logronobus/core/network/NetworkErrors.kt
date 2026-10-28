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

package dev.adrc95.logronobus.core.network

import dev.adrc95.logronobus.core.common.exception.AppError
import dev.adrc95.logronobus.core.common.exception.AppErrorException
import dev.adrc95.logronobus.core.common.exception.Result
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

suspend fun <T> tryCall(action: suspend () -> T): Result<T> =
    try {
        Result.Success(action.invoke())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toError())
    }

private fun Throwable.toError(): AppError =
    when (this) {
        is AppErrorException -> error
        is IOException -> AppError.Connectivity
        is ResponseException -> AppError.Server(response.status.value)
        else -> AppError.Unknown(message.orEmpty())
    }
